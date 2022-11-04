package com.basic.myrestapi.lectures;

import com.basic.myrestapi.accounts.Account;
import com.basic.myrestapi.accounts.CurrentUser;
import com.basic.myrestapi.common.ErrorsResource;
import com.basic.myrestapi.common.exception.BusinessException;
import com.basic.myrestapi.lectures.dto.LectureReqDto;
import com.basic.myrestapi.lectures.dto.LectureResDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    //constructor injection
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @PostMapping
    public ResponseEntity createLecture(@RequestBody @Valid LectureReqDto lectureReqDto,
                                        Errors errors,
                                        @CurrentUser Account account)
            throws Exception {
        //필드 검증
        if (errors.hasErrors()) {
            return badRequest(errors);
        }
        //로직 검증
        this.lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        // LectureReqDto => Lecture
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //free와 offline 값 Set
        lecture.update();
        //account 정보 set
        lecture.setAccount(account);
        Lecture addLecture = lectureRepository.save(lecture);
        // Lecture => LectureResDto
        LectureResDto lectureResDto = modelMapper.map(addLecture, LectureResDto.class);

        //http://localhost:8080/api/lectures/10
        WebMvcLinkBuilder selfLinkBuilder = linkTo(LectureController.class).slash(lectureResDto.getId());
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @GetMapping
    public ResponseEntity queryLectures(Pageable pageable,
                                        PagedResourcesAssembler<LectureResDto> assembler,
                                        @CurrentUser Account account)
            throws Exception {
        //return ResponseEntity.ok(this.lectureRepository.findAll(pageable));
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        Page<LectureResDto> lectureResDtoPage = lecturePage.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        //1단계 - prev,next 링크 제공
        //PagedModel pagedModel = assembler.toModel(lectureResDtoPage);
        //2단계 - prev,next 링크와 Self 링크 함께 제공
        /*
        PagedResourcesAssembler 클래스의 toModel() 메서드
        public <R extends org.springframework.hateoas.RepresentationModel<?>> org.springframework.hateoas.PagedModel<R> 
           toModel(Page<T> page, org.springframework.hateoas.server.RepresentationModelAssembler<T,R> assembler)
           T : LectureResDto, R : LectureResource

           RepresentationModelAssembler<T,R> 함수형 인터페이스의 추상메서드
           D toModel(T entity)
         */
//        PagedModel<LectureResource> pagedResources =
//                assembler.toModel(lectureResDtoPage, lectureResDto -> new LectureResource(lectureResDto));

        PagedModel<LectureResource> pagedResources =
                assembler.toModel(lectureResDtoPage, LectureResource::new);

        //인증토큰이 있다면 insert 할 수 있는 링크 제공
        if(account != null) {
            pagedResources.add(linkTo(LectureController.class).withRel("create-lecture"));
        }
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getLecture(@PathVariable Integer id, @CurrentUser Account currentUser) throws Exception {
//        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
//        if(optionalLecture.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        Lecture lecture = optionalLecture.get();

        //2. id와 매핑하는 Lecture가 없다면 BusinessException 404 오류를 발생시킨다.
        Lecture lecture = this.lectureRepository
                .findById(id) //Optional<Lecture>
                .orElseThrow(() -> new BusinessException(id + " Lecture Not Found", HttpStatus.NOT_FOUND));

        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        LectureResource lectureResource = new LectureResource(lectureResDto);
        //인증토큰이 있다면 update 할 수 있는 링크 제공
        if((lecture.getAccount() != null) && (lecture.getAccount().equals(currentUser))) {
            lectureResource.add(linkTo(LectureController.class)
                    .slash(lecture.getId()).withRel("update-lecture"));
        }
        return ResponseEntity.ok(lectureResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateLecture(@PathVariable Integer id,
                                        @RequestBody @Valid LectureReqDto lectureDto,
                                        Errors errors,
                                        @CurrentUser Account currentUser) {
        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
        if (optionalLecture.isEmpty()) {
            //return ResponseEntity.notFound().build();
            throw new BusinessException(id + " Lecture Not Found", HttpStatus.NOT_FOUND);
        }
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        this.lectureValidator.validate(lectureDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Lecture existingLecture = optionalLecture.get();

        //등록한 사용자와 갱신하는 사용자가 다르면 권한 없음 오류 발생
        if((existingLecture.getAccount() != null) &&
                (!existingLecture.getAccount().equals(currentUser))) {
            //return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            throw new BusinessException(id + " Lecture를 등록한 사용자와 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(lectureDto, existingLecture);
        Lecture savedLecture = this.lectureRepository.save(existingLecture);
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);
        LectureResource LectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(LectureResource);
    }
}
