import React, { useState, useEffect } from 'react';

function CourseList({ activeStudent, onStudentChange }) {
  const [courses, setCourses] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    fetchCourses();
    if (activeStudent) {
      fetchEnrollments();
    }
  }, [activeStudent]);

  const fetchCourses = async () => {
    try {
      const response = await fetch('http://localhost:8001/api/courses');
      const data = await response.json();
      setCourses(data);
    } catch (error) {
      console.error('Failed to fetch courses:', error);
    }
  };

  const fetchEnrollments = async () => {
    if (!activeStudent) return;
    try {
      const response = await fetch(`http://localhost:8001/api/enrollments/student/${activeStudent.studentId}`);
      const data = await response.json();
      setEnrollments(data);
    } catch (error) {
      console.error('Failed to fetch enrollments:', error);
    }
  };

  const getCourseStatus = (course) => {
    if (!activeStudent) return null;
    
    if (activeStudent.completedCourses?.includes(course.courseId)) {
      return 'completed';
    }
    
    const enrollment = enrollments.find(e => e.courseId === course.courseId && e.status === 'ENROLLED');
    if (enrollment) {
      return 'enrolled';
    }
    
    return 'available';
  };

  const handleEnroll = async (courseId) => {
    if (!activeStudent) {
      setMessage({ type: 'error', text: '请先选择一个学生' });
      return;
    }

    try {
      const response = await fetch('http://localhost:8001/api/enrollments/enroll', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: activeStudent.studentId,
          courseId: courseId
        })
      });

      const data = await response.json();

      if (response.status === 202) {
        setMessage({ type: 'warning', text: data.message });
      } else if (response.ok) {
        setMessage({ type: 'success', text: '选课成功!' });
        fetchEnrollments();
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '选课失败: ' + error.message });
    }
  };

  const handleWithdraw = async (courseId) => {
    if (!activeStudent) return;

    try {
      const response = await fetch('http://localhost:8001/api/enrollments/withdraw', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: activeStudent.studentId,
          courseId: courseId
        })
      });

      const data = await response.json();

      if (response.ok) {
        setMessage({ type: 'success', text: '退课成功!' });
        fetchEnrollments();
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '退课失败: ' + error.message });
    }
  };

  const handleCompleteCourse = async (courseId, score = null) => {
    if (!activeStudent) return;

    try {
      let url = `http://localhost:8001/api/grades/complete/${activeStudent.studentId}/${courseId}`;
      let options = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      };

      if (score !== null) {
        url = 'http://localhost:8001/api/grades/complete';
        options.body = JSON.stringify({
          studentId: activeStudent.studentId,
          courseId: courseId,
          score: score
        });
      }

      const response = await fetch(url, options);
      const data = await response.json();

      if (response.ok) {
        setMessage({ type: 'success', text: `课程完成成功! 成绩: ${data.score}分` });
        fetchEnrollments();
        fetchCourses();
        if (onStudentChange && activeStudent) {
          onStudentChange(activeStudent.studentId);
        }
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '完成课程失败: ' + error.message });
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'REQUIRED': return '#1976d2';
      case 'ELECTIVE': return '#388e3c';
      case 'GENERAL_EDUCATION': return '#f57c00';
      default: return '#757575';
    }
  };

  const getTypeLabel = (type) => {
    switch (type) {
      case 'REQUIRED': return '必修课';
      case 'ELECTIVE': return '选修课';
      case 'GENERAL_EDUCATION': return '通识课';
      default: return type;
    }
  };

  const getDayLabel = (day) => {
    const days = {
      'MONDAY': '周一',
      'TUESDAY': '周二',
      'WEDNESDAY': '周三',
      'THURSDAY': '周四',
      'FRIDAY': '周五',
      'SATURDAY': '周六',
      'SUNDAY': '周日'
    };
    return days[day] || day;
  };

  const cardStyle = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    marginBottom: '20px'
  };

  const capacityBar = (current, capacity) => {
    const percentage = (current / capacity) * 100;
    const color = percentage >= 100 ? '#d32f2f' : percentage >= 80 ? '#f57c00' : '#388e3c';
    return (
      <div style={{ width: '100%', height: '8px', backgroundColor: '#e0e0e0', borderRadius: '4px', overflow: 'hidden', marginTop: '8px' }}>
        <div style={{ width: `${Math.min(percentage, 100)}%`, height: '100%', backgroundColor: color }}></div>
      </div>
    );
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'completed':
        return { text: '已完成', color: '#2e7d32', bg: '#e8f5e9' };
      case 'enrolled':
        return { text: '已选', color: '#1976d2', bg: '#e3f2fd' };
      case 'available':
        return { text: '可选', color: '#757575', bg: '#f5f5f5' };
      default:
        return null;
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ margin: 0, color: '#333' }}>课程列表</h1>
        {activeStudent && (
          <div style={{ color: '#666' }}>
            当前学生: <strong>{activeStudent.name}</strong> ({activeStudent.studentId})
          </div>
        )}
      </div>

      <div style={{ marginBottom: '20px', display: 'flex', flexWrap: 'wrap', gap: '20px', alignItems: 'center' }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ width: '16px', height: '16px', backgroundColor: '#1976d2', borderRadius: '2px' }}></span>
          必修课
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ width: '16px', height: '16px', backgroundColor: '#388e3c', borderRadius: '2px' }}></span>
          选修课
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ width: '16px', height: '16px', backgroundColor: '#f57c00', borderRadius: '2px' }}></span>
          通识课
        </span>
        <span style={{ borderLeft: '1px solid #ccc', paddingLeft: '20px' }}></span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ width: '16px', height: '16px', backgroundColor: '#e8f5e9', border: '1px solid #2e7d32', borderRadius: '2px' }}></span>
          已完成
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ width: '16px', height: '16px', backgroundColor: '#e3f2fd', border: '1px solid #1976d2', borderRadius: '2px' }}></span>
          已选
        </span>
      </div>

      {message && (
        <div style={{
          padding: '12px 20px',
          borderRadius: '4px',
          marginBottom: '20px',
          backgroundColor: message.type === 'success' ? '#e8f5e9' : message.type === 'warning' ? '#fff3e0' : '#ffebee',
          color: message.type === 'success' ? '#2e7d32' : message.type === 'warning' ? '#ef6c00' : '#c62828'
        }}>
          {message.text}
        </div>
      )}

      {courses.map(course => {
        const status = getCourseStatus(course);
        const badge = getStatusBadge(status);
        
        return (
          <div 
            key={course.courseId} 
            style={{
              ...cardStyle,
              borderLeft: badge ? `4px solid ${badge.color}` : 'none'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px', flexWrap: 'wrap' }}>
                  <h3 style={{ margin: 0, color: '#333' }}>{course.courseName}</h3>
                  <span style={{
                    backgroundColor: getTypeColor(course.type),
                    color: 'white',
                    padding: '4px 12px',
                    borderRadius: '4px',
                    fontSize: '12px'
                  }}>
                    {getTypeLabel(course.type)}
                  </span>
                  {badge && (
                    <span style={{
                      backgroundColor: badge.bg,
                      color: badge.color,
                      padding: '4px 12px',
                      borderRadius: '4px',
                      fontSize: '12px',
                      border: `1px solid ${badge.color}`,
                      fontWeight: '600'
                    }}>
                      {badge.text}
                    </span>
                  )}
                </div>
                <div style={{ color: '#666', marginBottom: '12px' }}>
                  <strong>{course.courseId}</strong> | 学分: {course.credits} | 教师: {course.instructor}
                </div>
                
                <div style={{ marginBottom: '12px' }}>
                  <strong>上课时间:</strong>
                  <ul style={{ margin: '8px 0 0 20px', padding: 0 }}>
                    {course.schedule?.map((slot, idx) => (
                      <li key={idx} style={{ marginBottom: '4px' }}>
                        {getDayLabel(slot.day)} {slot.startHour}:{slot.startMinute.toString().padStart(2, '0')} - {slot.endHour}:{slot.endMinute.toString().padStart(2, '0')} ({slot.classroom})
                      </li>
                    ))}
                  </ul>
                </div>

                {course.prerequisites?.length > 0 && (
                  <div style={{ marginTop: '12px' }}>
                    <strong>先修课程:</strong>
                    <span style={{ color: '#d32f2f', marginLeft: '8px' }}>
                      {course.prerequisites.join(', ')}
                    </span>
                  </div>
                )}
              </div>
              
              <div style={{ width: '220px', marginLeft: '20px' }}>
                <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
                  容量: {course.enrolledCount}/{course.capacity}
                  {course.enrolledCount >= course.capacity && (
                    <span style={{ color: '#d32f2f', marginLeft: '8px' }}>(已满)</span>
                  )}
                </div>
                {capacityBar(course.enrolledCount, course.capacity)}
                
                {activeStudent && status !== 'completed' && (
                  <div style={{ marginTop: '16px' }}>
                    {status === 'enrolled' ? (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            style={{
                              flex: 1,
                              padding: '8px 12px',
                              backgroundColor: '#388e3c',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontWeight: '500',
                              fontSize: '13px'
                            }}
                            onClick={() => {
                              const score = prompt('请输入成绩 (0-100，默认85分):', '85');
                              if (score !== null) {
                                const scoreNum = parseFloat(score);
                                if (!isNaN(scoreNum) && scoreNum >= 0 && scoreNum <= 100) {
                                  handleCompleteCourse(course.courseId, scoreNum);
                                } else {
                                  alert('请输入有效的成绩 (0-100)');
                                }
                              }
                            }}
                            title="标记课程完成，自动录入成绩"
                          >
                            完成课程
                          </button>
                          <button
                            style={{
                              flex: 1,
                              padding: '8px 12px',
                              backgroundColor: '#d32f2f',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontWeight: '500',
                              fontSize: '13px'
                            }}
                            onClick={() => handleWithdraw(course.courseId)}
                            title="退选该课程"
                          >
                            退课
                          </button>
                        </div>
                        <div style={{ fontSize: '11px', color: '#757575', textAlign: 'center' }}>
                          提示：点击"完成课程"可录入成绩并标记完成
                        </div>
                      </div>
                    ) : (
                      <button
                        style={{
                          width: '100%',
                          padding: '8px 16px',
                          backgroundColor: course.enrolledCount >= course.capacity ? '#757575' : '#1976d2',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontWeight: '500'
                        }}
                        onClick={() => handleEnroll(course.courseId)}
                      >
                        {course.enrolledCount >= course.capacity ? '加入候补' : '选课'}
                      </button>
                    )}
                  </div>
                )}
                
                {activeStudent && status === 'completed' && (
                  <div style={{ marginTop: '16px', textAlign: 'center', color: '#2e7d32', fontWeight: '500' }}>
                    ✓ 课程已完成
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default CourseList;
