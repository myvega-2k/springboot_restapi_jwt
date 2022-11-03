package com.basic.myrestapi.lectures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LectureTest {
    @Test
    public void builder() {
        Lecture lecture = Lecture.builder()
                .name("스프링부트")
                .description("스프링부트를 사용한 API작성하기")
                .build();

        assertEquals("스프링부트", lecture.getName());

    }
}