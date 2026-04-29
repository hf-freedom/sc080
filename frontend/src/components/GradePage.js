import React, { useState, useEffect } from 'react';

function GradePage({ activeStudent }) {
  const [grades, setGrades] = useState([]);
  const [makeupExams, setMakeupExams] = useState([]);
  const [courses, setCourses] = useState([]);
  const [students, setStudents] = useState([]);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [selectedCourse, setSelectedCourse] = useState('');
  const [score, setScore] = useState('');
  const [message, setMessage] = useState(null);
  const [viewMode, setViewMode] = useState('view');

  useEffect(() => {
    fetchData();
  }, [activeStudent]);

  const fetchData = async () => {
    try {
      const [coursesRes, studentsRes] = await Promise.all([
        fetch('http://localhost:8001/api/courses'),
        fetch('http://localhost:8001/api/students')
      ]);
      const coursesData = await coursesRes.json();
      const studentsData = await studentsRes.json();
      setCourses(coursesData);
      setStudents(studentsData);

      if (activeStudent) {
        const [gradesRes, makeupRes] = await Promise.all([
          fetch(`http://localhost:8001/api/grades/student/${activeStudent.studentId}`),
          fetch(`http://localhost:8001/api/grades/student/${activeStudent.studentId}/makeup-exams`)
        ]);
        const gradesData = await gradesRes.json();
        const makeupData = await makeupRes.json();
        setGrades(gradesData);
        setMakeupExams(makeupData);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    }
  };

  const getCourseName = (courseId) => {
    const course = courses.find(c => c.courseId === courseId);
    return course ? course.courseName : courseId;
  };

  const handleEnterGrade = async () => {
    if (!selectedStudent || !selectedCourse || score === '') {
      setMessage({ type: 'error', text: '请填写完整信息' });
      return;
    }

    const scoreNum = parseFloat(score);
    if (isNaN(scoreNum) || scoreNum < 0 || scoreNum > 100) {
      setMessage({ type: 'error', text: '成绩必须在0-100之间' });
      return;
    }

    try {
      const response = await fetch('http://localhost:8001/api/grades/enter', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: selectedStudent.studentId,
          courseId: selectedCourse,
          score: scoreNum
        })
      });

      const data = await response.json();

      if (response.ok) {
        setMessage({ type: 'success', text: '成绩录入成功!' });
        setScore('');
        setSelectedCourse('');
        fetchData();
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '录入失败: ' + error.message });
    }
  };

  const handleEnterMakeup = async (examId, makeupScore) => {
    const exam = makeupExams.find(e => e.examId === examId);
    if (!exam) return;

    const scoreNum = parseFloat(makeupScore);
    if (isNaN(scoreNum) || scoreNum < 0 || scoreNum > 100) {
      setMessage({ type: 'error', text: '成绩必须在0-100之间' });
      return;
    }

    try {
      const response = await fetch('http://localhost:8001/api/grades/makeup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: exam.studentId,
          courseId: exam.courseId,
          score: scoreNum
        })
      });

      const data = await response.json();

      if (response.ok) {
        setMessage({ type: 'success', text: '补考成绩录入成功!' });
        fetchData();
      } else {
        setMessage({ type: 'error', text: data.error });
      }
    } catch (error) {
      setMessage({ type: 'error', text: '录入失败: ' + error.message });
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

  const getScoreColor = (score) => {
    if (score >= 90) return '#2e7d32';
    if (score >= 80) return '#1976d2';
    if (score >= 60) return '#f57c00';
    return '#d32f2f';
  };

  if (!activeStudent && viewMode === 'view') {
    return (
      <div>
        <h1 style={{ marginBottom: '20px', color: '#333' }}>成绩管理</h1>
        <div style={{ ...cardStyle, textAlign: 'center', color: '#666' }}>
          <h3>请先在导航栏中选择一个学生</h3>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ margin: 0, color: '#333' }}>成绩管理</h1>
        <div>
          <button
            style={{ padding: '8px 16px', backgroundColor: viewMode === 'view' ? '#1976d2' : '#757575', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '8px' }}
            onClick={() => setViewMode('view')}
          >
            查看成绩
          </button>
          <button
            style={{ padding: '8px 16px', backgroundColor: viewMode === 'enter' ? '#1976d2' : '#757575', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
            onClick={() => setViewMode('enter')}
          >
            录入成绩
          </button>
        </div>
      </div>

      {message && (
        <div style={{
          ...messageStyle,
          backgroundColor: message.type === 'success' ? '#e8f5e9' : '#ffebee',
          color: message.type === 'success' ? '#2e7d32' : '#c62828'
        }}>
          {message.text}
        </div>
      )}

      {viewMode === 'enter' ? (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 15px 0' }}>录入成绩</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px', alignItems: 'end' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>选择学生</label>
              <select
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                value={selectedStudent?.studentId || ''}
                onChange={(e) => {
                  const student = students.find(s => s.studentId === e.target.value);
                  setSelectedStudent(student);
                }}
              >
                <option value="">请选择学生</option>
                {students.map(s => (
                  <option key={s.studentId} value={s.studentId}>
                    {s.name} ({s.studentId})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>选择课程</label>
              <select
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                value={selectedCourse}
                onChange={(e) => setSelectedCourse(e.target.value)}
              >
                <option value="">请选择课程</option>
                {courses.map(c => (
                  <option key={c.courseId} value={c.courseId}>
                    {c.courseName} ({c.courseId})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px' }}>成绩 (0-100)</label>
              <input
                type="number"
                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
                min="0"
                max="100"
                value={score}
                onChange={(e) => setScore(e.target.value)}
              />
            </div>
            <div>
              <button
                style={{ padding: '8px 24px', backgroundColor: '#1976d2', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                onClick={handleEnterGrade}
              >
                录入
              </button>
            </div>
          </div>
        </div>
      ) : (
        <>
          <div style={{ marginBottom: '30px' }}>
            <h2 style={{ marginBottom: '15px' }}>成绩列表</h2>
            {grades.length === 0 ? (
              <div style={{ ...cardStyle, textAlign: 'center', color: '#666' }}>
                暂无成绩记录
              </div>
            ) : (
              <table style={tableStyle}>
                <thead>
                  <tr>
                    <th style={thStyle}>课程编号</th>
                    <th style={thStyle}>课程名称</th>
                    <th style={thStyle}>分数</th>
                    <th style={thStyle}>等级</th>
                    <th style={thStyle}>绩点</th>
                    <th style={thStyle}>获得学分</th>
                    <th style={thStyle}>状态</th>
                  </tr>
                </thead>
                <tbody>
                  {grades.map(grade => (
                    <tr key={grade.gradeId}>
                      <td style={tdStyle}>{grade.courseId}</td>
                      <td style={tdStyle}>{getCourseName(grade.courseId)}</td>
                      <td style={{ ...tdStyle, fontWeight: 'bold', color: getScoreColor(grade.score) }}>
                        {grade.score}
                      </td>
                      <td style={tdStyle}>{grade.letterGrade}</td>
                      <td style={tdStyle}>{grade.gradePoints}</td>
                      <td style={tdStyle}>
                        <span style={{ color: grade.creditsEarned ? '#2e7d32' : '#d32f2f' }}>
                          {grade.creditsEarned ? '是' : '否'}
                        </span>
                      </td>
                      <td style={tdStyle}>
                        {grade.status === 'MAKEUP' ? '补考通过' : '正常'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div>
            <h2 style={{ marginBottom: '15px' }}>补考资格</h2>
            {makeupExams.filter(e => e.status === 'ELIGIBLE' || e.status === 'SCHEDULED').length === 0 ? (
              <div style={{ ...cardStyle, textAlign: 'center', color: '#666' }}>
                暂无补考资格
              </div>
            ) : (
              <table style={tableStyle}>
                <thead>
                  <tr>
                    <th style={thStyle}>课程编号</th>
                    <th style={thStyle}>课程名称</th>
                    <th style={thStyle}>状态</th>
                    <th style={thStyle}>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {makeupExams.filter(e => e.status === 'ELIGIBLE' || e.status === 'SCHEDULED').map(exam => (
                    <tr key={exam.examId}>
                      <td style={tdStyle}>{exam.courseId}</td>
                      <td style={tdStyle}>{getCourseName(exam.courseId)}</td>
                      <td style={tdStyle}>{exam.status === 'ELIGIBLE' ? '有资格' : '已安排'}</td>
                      <td style={tdStyle}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <input
                            type="number"
                            style={{ padding: '4px', width: '60px', borderRadius: '4px', border: '1px solid #ccc' }}
                            min="0"
                            max="100"
                            placeholder="分数"
                            id={`makeup-score-${exam.examId}`}
                          />
                          <button
                            style={{ padding: '4px 12px', backgroundColor: '#1976d2', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                            onClick={() => {
                              const input = document.getElementById(`makeup-score-${exam.examId}`);
                              if (input && input.value) {
                                handleEnterMakeup(exam.examId, input.value);
                              }
                            }}
                          >
                            录入成绩
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default GradePage;
