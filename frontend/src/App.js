import React, { useState, useEffect } from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import StudentList from './components/StudentList';
import CourseList from './components/CourseList';
import EnrollmentPage from './components/EnrollmentPage';
import GradePage from './components/GradePage';
import GraduationPage from './components/GraduationPage';
import Home from './components/Home';

function App() {
  const [activeStudent, setActiveStudent] = useState(null);
  const [students, setStudents] = useState([]);

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      const response = await fetch('http://localhost:8001/api/students');
      const data = await response.json();
      setStudents(data);
      if (data.length > 0 && !activeStudent) {
        setActiveStudent(data[0]);
      }
    } catch (error) {
      console.error('Failed to fetch students:', error);
    }
  };

  const refreshStudent = async (studentId) => {
    try {
      const response = await fetch(`http://localhost:8001/api/students/${studentId}`);
      if (response.ok) {
        const data = await response.json();
        setActiveStudent(data);
        fetchStudents();
      }
    } catch (error) {
      console.error('Failed to refresh student:', error);
    }
  };

  const navStyle = {
    backgroundColor: '#1976d2',
    padding: '0 20px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
  };

  const ulStyle = {
    listStyle: 'none',
    display: 'flex',
    margin: 0,
    padding: 0
  };

  const linkStyle = {
    color: 'white',
    textDecoration: 'none',
    padding: '16px 20px',
    display: 'block',
    fontWeight: '500',
    transition: 'background-color 0.2s'
  };

  const contentStyle = {
    padding: '20px',
    maxWidth: '1400px',
    margin: '0 auto'
  };

  const selectStyle = {
    padding: '8px 12px',
    borderRadius: '4px',
    border: 'none',
    minWidth: '200px',
    fontSize: '14px'
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <nav style={navStyle}>
        <h1 style={{ color: 'white', margin: 0, fontSize: '20px' }}>高校选课系统</h1>
        <ul style={ulStyle}>
          <li><Link to="/" style={linkStyle}>首页</Link></li>
          <li><Link to="/students" style={linkStyle}>学生管理</Link></li>
          <li><Link to="/courses" style={linkStyle}>课程列表</Link></li>
          <li><Link to="/enrollment" style={linkStyle}>选课中心</Link></li>
          <li><Link to="/grades" style={linkStyle}>成绩管理</Link></li>
          <li><Link to="/graduation" style={linkStyle}>毕业审核</Link></li>
        </ul>
        {activeStudent && (
          <div style={{ color: 'white', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span>当前学生:</span>
            <select 
              style={selectStyle} 
              value={activeStudent.studentId}
              onChange={(e) => {
                const student = students.find(s => s.studentId === e.target.value);
                setActiveStudent(student);
              }}
            >
              {students.map(s => (
                <option key={s.studentId} value={s.studentId}>
                  {s.name} ({s.studentId})
                </option>
              ))}
            </select>
          </div>
        )}
      </nav>
      <div style={contentStyle}>
        <Routes>
          <Route path="/" element={<Home activeStudent={activeStudent} />} />
          <Route path="/students" element={<StudentList onStudentSelect={setActiveStudent} activeStudent={activeStudent} />} />
          <Route path="/courses" element={<CourseList activeStudent={activeStudent} onStudentChange={refreshStudent} />} />
          <Route path="/enrollment" element={<EnrollmentPage activeStudent={activeStudent} />} />
          <Route path="/grades" element={<GradePage activeStudent={activeStudent} />} />
          <Route path="/graduation" element={<GraduationPage activeStudent={activeStudent} />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
