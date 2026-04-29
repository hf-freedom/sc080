import React, { useState, useEffect } from 'react';

function EnrollmentPage({ activeStudent }) {
  const [courses, setCourses] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [waitlist, setWaitlist] = useState([]);
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (activeStudent) {
      fetchData();
    }
  }, [activeStudent]);

  const fetchData = async () => {
    try {
      const [coursesRes, enrollmentsRes] = await Promise.all([
        fetch('http://localhost:8001/api/courses'),
        fetch(`http://localhost:8001/api/enrollments/student/${activeStudent.studentId}`)
      ]);
      const coursesData = await coursesRes.json();
      const enrollmentsData = await enrollmentsRes.json();
      
      setCourses(coursesData);
      setEnrollments(enrollmentsData.filter(e => e.status === 'ENROLLED'));
    } catch (error) {
      console.error('Failed to fetch data:', error);
    }
  };

  const fetchWaitlist = async (courseId) => {
    try {
      const response = await fetch(`http://localhost:8001/api/enrollments/waitlist/course/${courseId}`);
      const data = await response.json();
      return data;
    } catch (error) {
      return [];
    }
  };

  const handleEnroll = async (courseId) => {
    if (!activeStudent) {
      setMessage({ type: 'error', text: '请先选择一个学生' });
      return;
    }

    setLoading(true);
    setMessage(null);

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
      } else {
        setMessage({ type: 'error', text: data.error });
      }

      fetchData();
    } catch (error) {
      setMessage({ type: 'error', text: '选课失败: ' + error.message });
    } finally {
      setLoading(false);
    }
  };

  const handleWithdraw = async (courseId) => {
    if (!activeStudent) return;

    setLoading(true);
    setMessage(null);

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
      } else {
        setMessage({ type: 'error', text: data.error });
      }

      fetchData();
    } catch (error) {
      setMessage({ type: 'error', text: '退课失败: ' + error.message });
    } finally {
      setLoading(false);
    }
  };

  const isEnrolled = (courseId) => {
    return enrollments.some(e => e.courseId === courseId);
  };

  const isCompleted = (courseId) => {
    return activeStudent?.completedCourses?.includes(courseId);
  };

  const cardStyle = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    marginBottom: '15px'
  };

  const buttonStyle = {
    padding: '8px 16px',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontWeight: '500',
    marginRight: '8px'
  };

  const messageStyle = {
    padding: '12px 20px',
    borderRadius: '4px',
    marginBottom: '20px'
  };

  const sectionHeader = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '15px'
  };

  if (!activeStudent) {
    return (
      <div>
        <h1 style={{ marginBottom: '20px', color: '#333' }}>选课中心</h1>
        <div style={{ ...cardStyle, textAlign: 'center', color: '#666' }}>
          <h3>请先在导航栏中选择一个学生</h3>
        </div>
      </div>
    );
  }

  const availableCourses = courses.filter(c => !isEnrolled(c.courseId) && !isCompleted(c.courseId));
  const enrolledCourses = courses.filter(c => isEnrolled(c.courseId));

  return (
    <div>
      <h1 style={{ marginBottom: '20px', color: '#333' }}>选课中心</h1>
      
      {message && (
        <div style={{
          ...messageStyle,
          backgroundColor: message.type === 'success' ? '#e8f5e9' : message.type === 'warning' ? '#fff3e0' : '#ffebee',
          color: message.type === 'success' ? '#2e7d32' : message.type === 'warning' ? '#ef6c00' : '#c62828'
        }}>
          {message.text}
        </div>
      )}

      <div style={{ marginBottom: '30px' }}>
        <div style={sectionHeader}>
          <h2 style={{ margin: 0 }}>已选课程 ({enrolledCourses.length})</h2>
        </div>
        {enrolledCourses.length === 0 ? (
          <div style={{ ...cardStyle, textAlign: 'center', color: '#666' }}>
            暂无已选课程
          </div>
        ) : (
          enrolledCourses.map(course => (
            <div key={course.courseId} style={cardStyle}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3 style={{ margin: '0 0 8px 0' }}>{course.courseName}</h3>
                  <div style={{ color: '#666' }}>
                    {course.courseId} | 学分: {course.credits} | 教师: {course.instructor}
                  </div>
                </div>
                <button
                  style={{ ...buttonStyle, backgroundColor: '#d32f2f', color: 'white' }}
                  onClick={() => handleWithdraw(course.courseId)}
                  disabled={loading}
                >
                  退课
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      <div>
        <div style={sectionHeader}>
          <h2 style={{ margin: 0 }}>可选课程 ({availableCourses.length})</h2>
        </div>
        {availableCourses.map(course => {
          const isFull = course.enrolledCount >= course.capacity;
          return (
            <div key={course.courseId} style={cardStyle}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <h3 style={{ margin: '0 0 8px 0' }}>
                    {course.courseName}
                    {isFull && <span style={{ color: '#d32f2f', marginLeft: '8px', fontSize: '14px' }}>(已满)</span>}
                  </h3>
                  <div style={{ color: '#666', marginBottom: '8px' }}>
                    {course.courseId} | 学分: {course.credits} | 教师: {course.instructor} | 
                    容量: {course.enrolledCount}/{course.capacity}
                  </div>
                  {course.prerequisites?.length > 0 && (
                    <div style={{ color: '#f57c00', fontSize: '14px' }}>
                      先修课程: {course.prerequisites.join(', ')}
                    </div>
                  )}
                </div>
                <button
                  style={{ 
                    ...buttonStyle, 
                    backgroundColor: isFull ? '#757575' : '#1976d2', 
                    color: 'white' 
                  }}
                  onClick={() => handleEnroll(course.courseId)}
                  disabled={loading}
                >
                  {isFull ? '加入候补' : '选课'}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default EnrollmentPage;
