package com.basic.myrestapi.lectures;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

@Controller
@RequestMapping(value="/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;

    //constructor injection
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @PostMapping
    public ResponseEntity createLecture(@RequestBody Lecture lecture) {
        Lecture addLecture = lectureRepository.save(lecture);

        //http://localhost:8080/api/lectures/10
        WebMvcLinkBuilder selfLinkBuilder = WebMvcLinkBuilder.linkTo(LectureController.class).slash(addLecture.getId());
        URI createUri = selfLinkBuilder.toUri();
        return ResponseEntity.created(createUri).body(addLecture);
    }

}
