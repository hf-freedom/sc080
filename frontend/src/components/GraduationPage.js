import React, { useState, useEffect } from 'react';

function GraduationPage({ activeStudent }) {
  const [reviewResult, setReviewResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [requiredCourses, setRequiredCourses] = useState([]);

  useEffect(() => {
    fetchRequiredCourses();
  }, []);

  useEffect(() => {
    if (activeStudent) {
      setReviewResult(null);
      setMessage(null);
    }
  }, [activeStudent]);

  const fetchRequiredCourses = async () => {
    try {
      const response = await fetch('http://localhost:8001/api/graduation/required-courses');
      const data = await response.json();
      setRequiredCourses(data);
    } catch (error) {
      console.error('Failed to fetch required courses:', error);
    }
  };

  const performReview = async () => {
    if (!activeStudent) {
      setMessage({ type: 'error', text: '请先选择一个学生' });
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const response = await fetch(`http://localhost:8001/api/graduation/review/${activeStudent.studentId}`, {
        method: 'POST'
      });

      const data = await response.json();

      if (response.ok) {
        setReviewResult(data);
        if (data.eligible) {
          setMessage({ type: 'success', text: '恭喜！该学生符合毕业资格！' });
        } else {
          setMessage({ type: 'error', text: '该学生不符合毕业资格' });
        }
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '审核失败: ' + error.message });
    } finally {
      setLoading(false);
    }
  };

  const viewResult = async () => {
    if (!activeStudent) {
      setMessage({ type: 'error', text: '请先选择一个学生' });
      return;
    }

    try {
      const response = await fetch(`http://localhost:8001/api/graduation/review/${activeStudent.studentId}?checkArrears=true`);
      
      if (response.status === 403) {
        const data = await response.json();
        setMessage({ type: 'error', text: data.error + '（欠费学生无法查看毕业审核结果）' });
        return;
      }

      if (response.status === 404) {
        setMessage({ type: 'warning', text: '暂无审核记录，请先执行审核' });
        return;
      }

      const data = await response.json();
      if (response.ok) {
        setReviewResult(data);
        setMessage(null);
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '获取结果失败: ' + error.message });
    }
  };

  const cardStyle = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    marginBottom: '20px'
  };

  const messageStyle = {
    padding: '12px 20px',
    borderRadius: '4px',
    marginBottom: '20px'
  };

  const buttonStyle = {
    padding: '10px 24px',
    backgroundColor: '#1976d2',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    marginRight: '12px',
    fontWeight: '500'
  };

  const statBox = (label, value, required, met) => ({
    backgroundColor: met ? '#e8f5e9' : '#ffebee',
    border: `1px solid ${met ? '#81c784' : '#e57373'}`,
    borderRadius: '8px',
    padding: '16px',
    textAlign: 'center'
  });

  if (!activeStudent) {
    return (
      <div>
        <h1 style={{ marginBottom: '20px', color: '#333' }}>毕业审核</h1>
        <div style={{ ...cardStyle, textAlign: 'center', color: '#666' }}>
          <h3>请先在导航栏中选择一个学生</h3>
        </div>
      </div>
    );
  }

  return (
    <div>
      <h1 style={{ marginBottom: '20px', color: '#333' }}>毕业审核</h1>

      {message && (
        <div style={{
          ...messageStyle,
          backgroundColor: message.type === 'success' ? '#e8f5e9' : message.type === 'warning' ? '#fff3e0' : '#ffebee',
          color: message.type === 'success' ? '#2e7d32' : message.type === 'warning' ? '#ef6c00' : '#c62828'
        }}>
          {message.text}
        </div>
      )}

      <div style={cardStyle}>
        <h3 style={{ margin: '0 0 15px 0' }}>学生信息</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px' }}>
          <div><strong>姓名:</strong> {activeStudent.name}</div>
          <div><strong>学号:</strong> {activeStudent.studentId}</div>
          <div><strong>专业:</strong> {activeStudent.major}</div>
          <div><strong>年级:</strong> {activeStudent.grade}</div>
          <div><strong>总学分:</strong> {activeStudent.totalCredits || 0}</div>
          <div><strong>GPA:</strong> {activeStudent.gpa || 0}</div>
          <div><strong>状态:</strong> {activeStudent.status}</div>
          <div style={{ color: activeStudent.hasArrears ? '#d32f2f' : '#2e7d32' }}>
            <strong>欠费:</strong> {activeStudent.hasArrears ? '是' : '否'}
          </div>
        </div>
      </div>

      <div style={{ ...cardStyle, display: 'flex', gap: '12px', alignItems: 'center' }}>
        <button
          style={buttonStyle}
          onClick={performReview}
          disabled={loading}
        >
          {loading ? '审核中...' : '执行毕业审核'}
        </button>
        <button
          style={{ ...buttonStyle, backgroundColor: '#757575' }}
          onClick={viewResult}
        >
          查看最近结果
        </button>
      </div>

      {reviewResult && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 20px 0' }}>
            审核结果: 
            <span style={{ 
              color: reviewResult.eligible ? '#2e7d32' : '#d32f2f',
              marginLeft: '10px'
            }}>
              {reviewResult.eligible ? '符合毕业资格' : '不符合毕业资格'}
            </span>
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px', marginBottom: '20px' }}>
            <div style={statBox('必修课学分', `${reviewResult.majorRequiredCreditsEarned}/${reviewResult.majorRequiredCreditsRequired}`, 
              reviewResult.majorRequiredCreditsRequired, reviewResult.majorRequiredCreditsEarned >= reviewResult.majorRequiredCreditsRequired)}>
              <div style={{ fontWeight: 'bold', fontSize: '14px', marginBottom: '4px' }}>必修课学分</div>
              <div style={{ fontSize: '18px' }}>{reviewResult.majorRequiredCreditsEarned}/{reviewResult.majorRequiredCreditsRequired}</div>
            </div>
            <div style={statBox('选修课学分', reviewResult.electiveCreditsEarned, 30, reviewResult.electiveCreditsEarned >= 30)}>
              <div style={{ fontWeight: 'bold', fontSize: '14px', marginBottom: '4px' }}>选修课学分</div>
              <div style={{ fontSize: '18px' }}>{reviewResult.electiveCreditsEarned}/30</div>
            </div>
            <div style={statBox('通识课学分', reviewResult.generalEdCreditsEarned, 30, reviewResult.generalEdCreditsEarned >= 30)}>
              <div style={{ fontWeight: 'bold', fontSize: '14px', marginBottom: '4px' }}>通识课学分</div>
              <div style={{ fontSize: '18px' }}>{reviewResult.generalEdCreditsEarned}/30</div>
            </div>
            <div style={statBox('GPA', reviewResult.gpa, reviewResult.minimumGpa, reviewResult.gpa >= reviewResult.minimumGpa)}>
              <div style={{ fontWeight: 'bold', fontSize: '14px', marginBottom: '4px' }}>GPA</div>
              <div style={{ fontSize: '18px' }}>{reviewResult.gpa}/{reviewResult.minimumGpa}</div>
            </div>
          </div>

          {reviewResult.failureReasons?.length > 0 && (
            <div style={{ backgroundColor: '#ffebee', padding: '15px', borderRadius: '4px', marginBottom: '15px' }}>
              <h4 style={{ margin: '0 0 10px 0', color: '#c62828' }}>不符合要求的原因:</h4>
              <ul style={{ margin: 0, paddingLeft: '20px', color: '#c62828' }}>
                {reviewResult.failureReasons.map((reason, idx) => (
                  <li key={idx}>{reason}</li>
                ))}
              </ul>
            </div>
          )}

          <div>
            <h4 style={{ margin: '0 0 10px 0' }}>必修课完成情况:</h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px' }}>
              {requiredCourses.map(courseId => {
                const completed = activeStudent.completedCourses?.includes(courseId);
                return (
                  <div 
                    key={courseId}
                    style={{
                      padding: '8px 12px',
                      backgroundColor: completed ? '#e8f5e9' : '#ffebee',
                      borderRadius: '4px',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <span>{courseId}</span>
                    <span style={{ color: completed ? '#2e7d32' : '#d32f2f', fontWeight: 'bold' }}>
                      {completed ? '已完成' : '未完成'}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default GraduationPage;
