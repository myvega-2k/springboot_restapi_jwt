package com.basic.myrestapi.lectures;

import java.time.LocalDateTime;

import com.basic.myrestapi.lectures.dto.LectureReqDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class LectureValidator {
	public void validate(LectureReqDto lectureReqDto, Errors errors) {
		//BasePrice가 MaxPrice보다 크면 에러로 체크
		if(lectureReqDto.getBasePrice() > lectureReqDto.getMaxPrice() &&
				lectureReqDto.getMaxPrice() != 0) {
			//Field Error
			errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
			errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
			//Global Error
			errors.reject("wrongPrices", "Values for prices are wrong");
		}
		
		LocalDateTime endLectureDateTime = lectureReqDto.getEndLectureDateTime();

		if(endLectureDateTime.isBefore(lectureReqDto.getBeginLectureDateTime()) ||
		   endLectureDateTime.isBefore(lectureReqDto.getCloseEnrollmentDateTime()) ||
		   endLectureDateTime.isBefore(lectureReqDto.getBeginEnrollmentDateTime()) ) {
			errors.rejectValue("endLectureDateTime", "wrongValue", "endLectureDateTime is wrong");
		}
	}
}