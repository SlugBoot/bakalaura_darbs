package lv.slugboot.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lv.slugboot.app.service.ICourseCRUDService;

@Controller
@RequestMapping("/course")
public class CourseCRUDController {

	@Autowired ICourseCRUDService courseCRUDService;
}
