import React, { useState, useEffect } from 'react';

function Home({ activeStudent }) {
  const [stats, setStats] = useState({ students: 0, courses: 0, enrollments: 0 });
  const [withdrawStatus, setWithdrawStatus] = useState(null);

  useEffect(() => {
    fetchStats();
    fetchWithdrawStatus();
  }, []);

  const fetchStats = async () => {
    try {
      const [studentsRes, coursesRes] = await Promise.all([
        fetch('http://localhost:8001/api/students'),
        fetch('http://localhost:8001/api/courses')
      ]);
      const students = await studentsRes.json();
      const courses = await coursesRes.json();
      setStats({
        students: students.length,
        courses: courses.length,
        enrollments: courses.reduce((sum, c) => sum + (c.enrolledCount || 0), 0)
      });
    } catch (error) {
      console.error('Failed to fetch stats:', error);
    }
  };

  const fetchWithdrawStatus = async () => {
    try {
      const response = await fetch('http://localhost:8001/api/enrollments/withdraw/status');
      const data = await response.json();
      setWithdrawStatus(data);
    } catch (error) {
      console.error('Failed to fetch withdraw status:', error);
    }
  };

  const cardStyle = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '24px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    textAlign: 'center'
  };

  const statNumber = {
    fontSize: '36px',
    fontWeight: 'bold',
    color: '#1976d2',
    margin: '8px 0'
  };

  const sectionStyle = {
    marginTop: '30px',
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '24px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
  };

  return (
    <div>
      <h1 style={{ marginBottom: '20px', color: '#333' }}>欢迎使用高校选课系统</h1>
      
      {activeStudent && (
        <div style={{ ...sectionStyle, backgroundColor: '#e3f2fd', marginBottom: '20px' }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#1565c0' }}>当前登录学生</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px' }}>
            <div><strong>姓名:</strong> {activeStudent.name}</div>
            <div><strong>学号:</strong> {activeStudent.studentId}</div>
            <div><strong>专业:</strong> {activeStudent.major}</div>
            <div><strong>年级:</strong> {activeStudent.grade}</div>
            <div><strong>学分:</strong> {activeStudent.totalCredits || 0}</div>
            <div><strong>GPA:</strong> {activeStudent.gpa || 0}</div>
            <div><strong>状态:</strong> {activeStudent.status}</div>
            <div style={{ color: activeStudent.hasArrears ? 'red' : 'green' }}>
              <strong>欠费:</strong> {activeStudent.hasArrears ? '是' : '否'}
            </div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
        <div style={cardStyle}>
          <div style={{ fontSize: '16px', color: '#666' }}>学生总数</div>
          <div style={statNumber}>{stats.students}</div>
        </div>
        <div style={cardStyle}>
          <div style={{ fontSize: '16px', color: '#666' }}>课程总数</div>
          <div style={statNumber}>{stats.courses}</div>
        </div>
        <div style={cardStyle}>
          <div style={{ fontSize: '16px', color: '#666' }}>选课人次</div>
          <div style={statNumber}>{stats.enrollments}</div>
        </div>
      </div>

      {withdrawStatus && (
        <div style={sectionStyle}>
          <h3 style={{ margin: '0 0 15px 0' }}>系统状态</h3>
          <div style={{ 
            padding: '12px 20px',
            backgroundColor: withdrawStatus.canWithdraw ? '#e8f5e9' : '#ffebee',
            borderRadius: '4px',
            color: withdrawStatus.canWithdraw ? '#2e7d32' : '#c62828'
          }}>
            <strong>退课状态:</strong> {withdrawStatus.canWithdraw ? '可以退课' : '退课截止时间已过'}
          </div>
        </div>
      )}

      <div style={sectionStyle}>
        <h3 style={{ margin: '0 0 15px 0' }}>系统功能说明</h3>
        <ul style={{ lineHeight: '2' }}>
          <li><strong>课程容量:</strong> 每门课程有最大容量限制，满员后学生可进入候补队列</li>
          <li><strong>先修课要求:</strong> 选课前需完成所有先修课程</li>
          <li><strong>时间冲突:</strong> 系统会自动检查上课时间是否冲突</li>
          <li><strong>候补队列:</strong> 课程满员后，学生可加入候补队列，有人退课时自动补位</li>
          <li><strong>退课截止:</strong> 超过截止日期后无法退课</li>
          <li><strong>成绩管理:</strong> 成绩录入后自动计算获得学分，不及格可参加补考</li>
          <li><strong>毕业审核:</strong> 检查必修课完成情况、选修课学分、GPA和欠费状态</li>
        </ul>
      </div>
    </div>
  );
}

export default Home;
