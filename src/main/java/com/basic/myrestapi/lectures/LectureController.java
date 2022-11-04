package com.basic.myrestapi.lectures;

import com.basic.myrestapi.common.ErrorsResource;
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
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value="/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
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
    public ResponseEntity createLecture(@RequestBody @Valid LectureReqDto lectureReqDto, Errors errors) {
        //필드 검증
        if(errors.hasErrors()) {
            return badRequest(errors);
        }
        //로직 검증
        this.lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()) {
            return badRequest(errors);
        }

        // LectureReqDto => Lecture
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //free와 offline 값 Set
        lecture.update();
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
    public ResponseEntity queryLectures(Pageable pageable, PagedResourcesAssembler assembler) {
        //return ResponseEntity.ok(this.lectureRepository.findAll(pageable));
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        Page<LectureResDto> lectureResDtoPage = lecturePage.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        //1단계 - prev,next 링크 제공
        //PagedModel pagedModel = assembler.toModel(lectureResDtoPage);
        //2단계 - prev,next 링크와 Self 링크 함께 제공
        /*
        PagedResourcesAssembler 클래스의 toModel() 메서드
        public <R extends org.springframework.hateoas.RepresentationModel<?>> org.springframework.hateoas.PagedModel<R> 
           toModel(Page<T> page,org.springframework.hateoas.server.RepresentationModelAssembler<T,R> assembler)
         */
        return ResponseEntity.ok(pagedModel);
    }
}
