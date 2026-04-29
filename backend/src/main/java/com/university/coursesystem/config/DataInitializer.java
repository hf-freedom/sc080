package com.university.coursesystem.config;

import com.university.coursesystem.model.*;
import com.university.coursesystem.repository.CourseRepository;
import com.university.coursesystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Override
    public void run(String... args) {
        initializeStudents();
        initializeCourses();
    }
    
    private void initializeStudents() {
        if (studentRepository.findAll().isEmpty()) {
            Student student1 = Student.builder()
                    .studentId("S2021001")
                    .name("张三")
                    .major("计算机科学与技术")
                    .grade(2021)
                    .status(StudentStatus.ENROLLED)
                    .totalCredits(95.0)
                    .gpa(3.5)
                    .hasArrears(false)
                    .completedCourses(Arrays.asList("CS101", "CS102", "CS201", "MATH101", "ENG101", "CSE101"))
                    .build();
            
            Student student2 = Student.builder()
                    .studentId("S2021002")
                    .name("李四")
                    .major("计算机科学与技术")
                    .grade(2021)
                    .status(StudentStatus.ENROLLED)
                    .totalCredits(82.0)
                    .gpa(2.8)
                    .hasArrears(false)
                    .completedCourses(Arrays.asList("CS101", "CS102", "CS201", "CS202", "MATH101", "ENG101"))
                    .build();
            
            Student student3 = Student.builder()
                    .studentId("S2021003")
                    .name("王五")
                    .major("软件工程")
                    .grade(2021)
                    .status(StudentStatus.ENROLLED)
                    .totalCredits(110.0)
                    .gpa(3.0)
                    .hasArrears(true)
                    .completedCourses(Arrays.asList("CS101", "CS102", "CS201", "CS202", "CS301", "MATH101", "ENG101", "CSE101"))
                    .build();
            
            Student student4 = Student.builder()
                    .studentId("S2022001")
                    .name("赵六")
                    .major("计算机科学与技术")
                    .grade(2022)
                    .status(StudentStatus.ENROLLED)
                    .totalCredits(70.0)
                    .gpa(3.8)
                    .hasArrears(false)
                    .completedCourses(Arrays.asList("CS101", "CS102", "CS201", "MATH101", "ENG101"))
                    .build();
            
            studentRepository.save(student1);
            studentRepository.save(student2);
            studentRepository.save(student3);
            studentRepository.save(student4);
        }
    }
    
    private void initializeCourses() {
        if (courseRepository.findAll().isEmpty()) {
            TimeSlot cs101Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.MONDAY)
                    .startHour(8)
                    .startMinute(0)
                    .endHour(9)
                    .endMinute(40)
                    .classroom("A101")
                    .build();
            
            TimeSlot cs101Slot2 = TimeSlot.builder()
                    .day(DayOfWeek.WEDNESDAY)
                    .startHour(8)
                    .startMinute(0)
                    .endHour(9)
                    .endMinute(40)
                    .classroom("A101")
                    .build();
            
            Course cs101 = Course.builder()
                    .courseId("CS101")
                    .courseName("计算机导论")
                    .credits(3)
                    .type(CourseType.REQUIRED)
                    .capacity(60)
                    .enrolledCount(45)
                    .instructor("张教授")
                    .schedule(Arrays.asList(cs101Slot1, cs101Slot2))
                    .prerequisites(Arrays.asList())
                    .build();
            
            TimeSlot cs102Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.TUESDAY)
                    .startHour(10)
                    .startMinute(0)
                    .endHour(11)
                    .endMinute(40)
                    .classroom("A201")
                    .build();
            
            TimeSlot cs102Slot2 = TimeSlot.builder()
                    .day(DayOfWeek.THURSDAY)
                    .startHour(10)
                    .startMinute(0)
                    .endHour(11)
                    .endMinute(40)
                    .classroom("A201")
                    .build();
            
            Course cs102 = Course.builder()
                    .courseId("CS102")
                    .courseName("程序设计基础")
                    .credits(4)
                    .type(CourseType.REQUIRED)
                    .capacity(50)
                    .enrolledCount(50)
                    .instructor("李教授")
                    .schedule(Arrays.asList(cs102Slot1, cs102Slot2))
                    .prerequisites(Arrays.asList("CS101"))
                    .build();
            
            TimeSlot cs201Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.MONDAY)
                    .startHour(14)
                    .startMinute(0)
                    .endHour(15)
                    .endMinute(40)
                    .classroom("B101")
                    .build();
            
            TimeSlot cs201Slot2 = TimeSlot.builder()
                    .day(DayOfWeek.WEDNESDAY)
                    .startHour(14)
                    .startMinute(0)
                    .endHour(15)
                    .endMinute(40)
                    .classroom("B101")
                    .build();
            
            Course cs201 = Course.builder()
                    .courseId("CS201")
                    .courseName("数据结构")
                    .credits(4)
                    .type(CourseType.REQUIRED)
                    .capacity(45)
                    .enrolledCount(40)
                    .instructor("王教授")
                    .schedule(Arrays.asList(cs201Slot1, cs201Slot2))
                    .prerequisites(Arrays.asList("CS102"))
                    .build();
            
            TimeSlot cs202Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.TUESDAY)
                    .startHour(14)
                    .startMinute(0)
                    .endHour(15)
                    .endMinute(40)
                    .classroom("B201")
                    .build();
            
            Course cs202 = Course.builder()
                    .courseId("CS202")
                    .courseName("算法设计与分析")
                    .credits(3)
                    .type(CourseType.REQUIRED)
                    .capacity(40)
                    .enrolledCount(35)
                    .instructor("赵教授")
                    .schedule(Arrays.asList(cs202Slot1))
                    .prerequisites(Arrays.asList("CS201"))
                    .build();
            
            TimeSlot cs301Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.FRIDAY)
                    .startHour(8)
                    .startMinute(0)
                    .endHour(9)
                    .endMinute(40)
                    .classroom("C101")
                    .build();
            
            Course cs301 = Course.builder()
                    .courseId("CS301")
                    .courseName("操作系统")
                    .credits(4)
                    .type(CourseType.REQUIRED)
                    .capacity(40)
                    .enrolledCount(30)
                    .instructor("钱教授")
                    .schedule(Arrays.asList(cs301Slot1))
                    .prerequisites(Arrays.asList("CS201", "CS102"))
                    .build();
            
            TimeSlot cs401Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.THURSDAY)
                    .startHour(14)
                    .startMinute(0)
                    .endHour(15)
                    .endMinute(40)
                    .classroom("D101")
                    .build();
            
            Course cs401 = Course.builder()
                    .courseId("CS401")
                    .courseName("计算机网络")
                    .credits(3)
                    .type(CourseType.REQUIRED)
                    .capacity(35)
                    .enrolledCount(30)
                    .instructor("孙教授")
                    .schedule(Arrays.asList(cs401Slot1))
                    .prerequisites(Arrays.asList("CS301"))
                    .build();
            
            TimeSlot math101Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.MONDAY)
                    .startHour(10)
                    .startMinute(0)
                    .endHour(11)
                    .endMinute(40)
                    .classroom("M101")
                    .build();
            
            TimeSlot math101Slot2 = TimeSlot.builder()
                    .day(DayOfWeek.WEDNESDAY)
                    .startHour(10)
                    .startMinute(0)
                    .endHour(11)
                    .endMinute(40)
                    .classroom("M101")
                    .build();
            
            Course math101 = Course.builder()
                    .courseId("MATH101")
                    .courseName("高等数学")
                    .credits(5)
                    .type(CourseType.GENERAL_EDUCATION)
                    .capacity(80)
                    .enrolledCount(70)
                    .instructor("周教授")
                    .schedule(Arrays.asList(math101Slot1, math101Slot2))
                    .prerequisites(Arrays.asList())
                    .build();
            
            TimeSlot eng101Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.TUESDAY)
                    .startHour(8)
                    .startMinute(0)
                    .endHour(9)
                    .endMinute(40)
                    .classroom("E101")
                    .build();
            
            TimeSlot eng101Slot2 = TimeSlot.builder()
                    .day(DayOfWeek.THURSDAY)
                    .startHour(8)
                    .startMinute(0)
                    .endHour(9)
                    .endMinute(40)
                    .classroom("E101")
                    .build();
            
            Course eng101 = Course.builder()
                    .courseId("ENG101")
                    .courseName("大学英语")
                    .credits(4)
                    .type(CourseType.GENERAL_EDUCATION)
                    .capacity(50)
                    .enrolledCount(48)
                    .instructor("吴教授")
                    .schedule(Arrays.asList(eng101Slot1, eng101Slot2))
                    .prerequisites(Arrays.asList())
                    .build();
            
            TimeSlot csE101Slot1 = TimeSlot.builder()
                    .day(DayOfWeek.FRIDAY)
                    .startHour(14)
                    .startMinute(0)
                    .endHour(15)
                    .endMinute(40)
                    .classroom("E201")
                    .build();
            
            Course csE101 = Course.builder()
                    .courseId("CSE101")
                    .courseName("人工智能导论")
                    .credits(2)
                    .type(CourseType.ELECTIVE)
                    .capacity(30)
                    .enrolledCount(28)
                    .instructor("郑教授")
                    .schedule(Arrays.asList(csE101Slot1))
                    .prerequisites(Arrays.asList("CS102"))
                    .build();
            
            courseRepository.save(cs101);
            courseRepository.save(cs102);
            courseRepository.save(cs201);
            courseRepository.save(cs202);
            courseRepository.save(cs301);
            courseRepository.save(cs401);
            courseRepository.save(math101);
            courseRepository.save(eng101);
            courseRepository.save(csE101);
        }
    }
}
