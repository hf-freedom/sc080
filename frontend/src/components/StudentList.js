import React, { useState, useEffect } from 'react';

function StudentList({ onStudentSelect, activeStudent }) {
  const [students, setStudents] = useState([]);
  const [editingStudent, setEditingStudent] = useState(null);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      const response = await fetch('http://localhost:8001/api/students');
      const data = await response.json();
      setStudents(data);
    } catch (error) {
      console.error('Failed to fetch students:', error);
    }
  };

  const handleStatusChange = async (studentId, hasArrears) => {
    try {
      const student = students.find(s => s.studentId === studentId);
      if (!student) return;

      const updatedStudent = { ...student, hasArrears };
      const response = await fetch(`http://localhost:8001/api/students/${studentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedStudent)
      });

      if (response.ok) {
        setMessage({ type: 'success', text: '学生状态更新成功' });
        fetchStudents();
      }
    } catch (error) {
      setMessage({ type: 'error', text: '更新失败: ' + error.message });
    }
  };

  const tableStyle = {
    width: '100%',
    borderCollapse: 'collapse',
    backgroundColor: 'white',
    borderRadius: '8px',
    overflow: 'hidden',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
  };

  const thStyle = {
    backgroundColor: '#1976d2',
    color: 'white',
    padding: '12px 16px',
    textAlign: 'left',
    fontWeight: '600'
  };

  const tdStyle = {
    padding: '12px 16px',
    borderBottom: '1px solid #e0e0e0'
  };

  const buttonStyle = {
    padding: '6px 12px',
    backgroundColor: '#1976d2',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    marginRight: '8px'
  };

  const dangerButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#d32f2f'
  };

  const messageStyle = {
    padding: '12px 20px',
    borderRadius: '4px',
    marginBottom: '20px'
  };

  return (
    <div>
      <h1 style={{ marginBottom: '20px', color: '#333' }}>学生管理</h1>
      
      {message && (
        <div style={{
          ...messageStyle,
          backgroundColor: message.type === 'success' ? '#e8f5e9' : '#ffebee',
          color: message.type === 'success' ? '#2e7d32' : '#c62828'
        }}>
          {message.text}
        </div>
      )}

      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>学号</th>
            <th style={thStyle}>姓名</th>
            <th style={thStyle}>专业</th>
            <th style={thStyle}>年级</th>
            <th style={thStyle}>学分</th>
            <th style={thStyle}>GPA</th>
            <th style={thStyle}>状态</th>
            <th style={thStyle}>欠费</th>
            <th style={thStyle}>操作</th>
          </tr>
        </thead>
        <tbody>
          {students.map(student => (
            <tr key={student.studentId} style={{
              backgroundColor: activeStudent?.studentId === student.studentId ? '#e3f2fd' : 'transparent'
            }}>
              <td style={tdStyle}>{student.studentId}</td>
              <td style={tdStyle}>{student.name}</td>
              <td style={tdStyle}>{student.major}</td>
              <td style={tdStyle}>{student.grade}</td>
              <td style={tdStyle}>{student.totalCredits || 0}</td>
              <td style={tdStyle}>{student.gpa || 0}</td>
              <td style={tdStyle}>{student.status}</td>
              <td style={tdStyle}>
                <span style={{
                  color: student.hasArrears ? '#d32f2f' : '#2e7d32',
                  fontWeight: 'bold'
                }}>
                  {student.hasArrears ? '是' : '否'}
                </span>
              </td>
              <td style={tdStyle}>
                <button 
                  style={buttonStyle}
                  onClick={() => onStudentSelect(student)}
                >
                  选择
                </button>
                <button
                  style={student.hasArrears ? buttonStyle : dangerButtonStyle}
                  onClick={() => handleStatusChange(student.studentId, !student.hasArrears)}
                >
                  {student.hasArrears ? '清除欠费' : '标记欠费'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default StudentList;
