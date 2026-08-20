import { useMemo, useState } from 'react'

import './AdminMembersPage.css'


const memberMockData = [
  {
    id: 1,
    name: '김회원',
    email: 'user1@example.com',
    role: 'USER',
    provider: 'LOCAL',
    status: '활성',
  },
  {
    id: 2,
    name: '이관리자',
    email: 'admin@example.com',
    role: 'ADMIN',
    provider: 'GOOGLE',
    status: '활성',
  },
  {
    id: 3,
    name: '박회원',
    email: 'user2@example.com',
    role: 'USER',
    provider: 'NAVER',
    status: '활성',
  },
]


const roleFilters = [
  '전체',
  'USER',
  'ADMIN',
]


function AdminMembersPage() {
  const [keyword, setKeyword] = useState('')
  const [roleFilter, setRoleFilter] =
    useState('전체')


  const filteredMembers = useMemo(() => {
    const normalizedKeyword =
      keyword.trim().toLowerCase()

    return memberMockData.filter((member) => {
      const matchesKeyword =
        !normalizedKeyword
        ||
        member.name
          .toLowerCase()
          .includes(normalizedKeyword)
        ||
        member.email
          .toLowerCase()
          .includes(normalizedKeyword)

      const matchesRole =
        roleFilter === '전체'
        ||
        member.role === roleFilter

      return matchesKeyword && matchesRole
    })
  }, [keyword, roleFilter])


  return (
    <main className="admin-members-page">

      <section className="admin-members-header">

        <div>
          <span className="admin-page-eyebrow">
            ADMIN / MEMBER
          </span>

          <h1>
            회원 및 권한 관리
          </h1>

          <p>
            회원 정보를 조회하고
            BloodOps 서비스 접근 권한을 관리합니다.
          </p>
        </div>

      </section>


      <section className="admin-member-toolbar">

        <div className="admin-member-search">
          <input
            type="search"
            value={keyword}
            onChange={(event) =>
              setKeyword(event.target.value)
            }
            placeholder="회원명 또는 이메일 검색"
            aria-label="회원 검색"
          />
        </div>


        <div
          className="admin-member-filter"
          aria-label="회원 권한 필터"
        >
          {roleFilters.map((role) => (
            <button
              key={role}
              type="button"
              className={
                roleFilter === role
                  ? 'active'
                  : ''
              }
              onClick={() =>
                setRoleFilter(role)
              }
            >
              {role}
            </button>
          ))}
        </div>


        <span className="admin-member-count">
          총 {filteredMembers.length}명
        </span>

      </section>


      <section className="admin-member-list">

        <div className="admin-member-list-header">
          <span>회원</span>
          <span>이메일</span>
          <span>가입 방식</span>
          <span>권한</span>
          <span>상태</span>
          <span>관리</span>
        </div>


        {filteredMembers.map((member) => (
          <article
            key={member.id}
            className="admin-member-row"
          >
            <strong>
              {member.name}
            </strong>

            <span>
              {member.email}
            </span>

            <span>
              {member.provider}
            </span>

            <span
              className={
                member.role === 'ADMIN'
                  ? 'admin-member-role admin'
                  : 'admin-member-role user'
              }
            >
              {member.role}
            </span>

            <span className="admin-member-status">
              {member.status}
            </span>

            <button
              type="button"
              className="admin-row-action"
            >
              권한 변경
            </button>
          </article>
        ))}

      </section>

    </main>
  )
}


export default AdminMembersPage