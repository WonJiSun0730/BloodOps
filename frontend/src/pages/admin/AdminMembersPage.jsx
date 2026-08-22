import { useEffect, useMemo, useState } from 'react'

import {
  getAdminMembers,
  getCurrentUser,
  updateAdminMemberRole,
} from '../../api/apiClient'

import ConfirmModal from '../../components/modal/ConfirmModal'

import AdminSectionNav
  from '../../components/admin/AdminSectionNav.jsx'

import './AdminMembersPage.css'


const roleFilters = [
  '전체',
  'USER',
  'MEDICAL',
  'ADMIN',
]


function AdminMembersPage() {
  const [members, setMembers] = useState([])
  const [currentUser, setCurrentUser] = useState(null)

  const [keyword, setKeyword] = useState('')
  const [roleFilter, setRoleFilter] =
    useState('전체')

  const [isLoading, setIsLoading] =
    useState(true)

  const [errorMessage, setErrorMessage] =
    useState('')

  const [roleDrafts, setRoleDrafts] =
    useState({})

  const [updatingMemberId, setUpdatingMemberId] =
    useState(null)

  const [confirmTarget, setConfirmTarget] =
    useState(null)

  const [notice, setNotice] =
    useState(null)

  useEffect(() => {
    let isMounted = true

    async function loadMembers() {
      try {
        setIsLoading(true)
        setErrorMessage('')

        const [
          memberData,
          currentUserData,
        ] = await Promise.all([
          getAdminMembers(),
          getCurrentUser(),
        ])

        if (isMounted) {
          setMembers(memberData)
          setCurrentUser(currentUserData)
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(
            error.message
            || '회원 목록을 불러오지 못했습니다.'
          )
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadMembers()

    return () => {
      isMounted = false
    }
  }, [])

    async function handleRoleChange(member) {
      const newRole =
        roleDrafts[member.id] ?? member.role

      if (newRole === member.role) {
        return
      }

      if (
        member.role === 'ADMIN'
        || newRole === 'ADMIN'
      ) {
        setConfirmTarget({
          member,
          newRole,
        })

        return
      }

      await executeRoleChange(
        member,
        newRole
      )
    }

    async function executeRoleChange(
      member,
      newRole
    ) {
      try {
        setUpdatingMemberId(member.id)

        const result =
          await updateAdminMemberRole(
            member.id,
            newRole
          )

        if (result.adminAccessLost) {
          window.location.href = '/admin/members'
          return
        }

        const updatedMember = result.member

        setMembers((currentMembers) =>
          currentMembers.map((currentMember) =>
            currentMember.id === updatedMember.id
              ? updatedMember
              : currentMember
          )
        )

        setRoleDrafts((currentDrafts) => {
          const nextDrafts = {
            ...currentDrafts,
          }

          delete nextDrafts[member.id]

          return nextDrafts
        })
      } catch (error) {
        setNotice({
          title: '권한 변경 실패',
          message:
            error.message
            || '권한 변경에 실패했습니다.',
        })

        setRoleDrafts((currentDrafts) => ({
          ...currentDrafts,
          [member.id]: member.role,
        }))
      } finally {
        setUpdatingMemberId(null)
      }
    }


    const filteredMembers = useMemo(() => {
    const normalizedKeyword =
      keyword.trim().toLowerCase()

    return members.filter((member) => {
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
  }, [members, keyword, roleFilter])


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

      <AdminSectionNav />

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
          <span>관리</span>
        </div>


        {isLoading && (
          <p>
            회원 목록을 불러오는 중입니다.
          </p>
        )}


        {errorMessage && (
          <p>
            {errorMessage}
          </p>
        )}


        {!isLoading
          && !errorMessage
          && filteredMembers.map((member) => (
          <article
            key={member.id}
            className="admin-member-row"
          >
            <strong>
              {member.name}

              {currentUser?.identifier === member.email && (
                <span className="admin-member-self">
                  본인
                </span>
              )}
            </strong>

            <span>
              {member.email}
            </span>

            <span>
              {member.provider}
            </span>

            <span
              className={
                `admin-member-role ${member.role.toLowerCase()}`
              }
            >
              {member.role}
            </span>

            <div className="admin-role-control">
              <select
                value={
                  roleDrafts[member.id]
                  ?? member.role
                }
                onChange={(event) =>
                  setRoleDrafts((currentDrafts) => ({
                    ...currentDrafts,
                    [member.id]: event.target.value,
                  }))
                }
                disabled={
                  updatingMemberId === member.id
                }
                aria-label={`${member.name} 권한 선택`}
              >
                <option value="USER">
                  USER
                </option>

                <option value="MEDICAL">
                  MEDICAL
                </option>

                <option value="ADMIN">
                  ADMIN
                </option>
              </select>

              <button
                type="button"
                className="admin-row-action"
                onClick={() =>
                  handleRoleChange(member)
                }
                disabled={
                  updatingMemberId === member.id
                  ||
                  (
                    roleDrafts[member.id]
                    ?? member.role
                  ) === member.role
                }
              >
                {updatingMemberId === member.id
                  ? '변경 중'
                  : '변경'}
              </button>
            </div>
          </article>
        ))}

      </section>

    <ConfirmModal
        open={confirmTarget !== null}
        title="관리자 권한 변경"
        message={
          confirmTarget
            ? `${confirmTarget.member.name} 회원의 권한을 `
              + `${confirmTarget.member.role} → `
              + `${confirmTarget.newRole}(으)로 변경합니다.\n`
              + '관리자 권한 변경은 서비스 접근 범위에 영향을 줍니다.'
            : ''
        }
        confirmText="권한 변경"
        cancelText="취소"
        danger
        onCancel={() =>
          setConfirmTarget(null)
        }
        onConfirm={async () => {
          const target = confirmTarget

          setConfirmTarget(null)

          if (!target) {
            return
          }

          await executeRoleChange(
            target.member,
            target.newRole
          )
        }}
      />


      <ConfirmModal
        open={notice !== null}
        title={
          notice?.title
          ?? '안내'
        }
        message={
          notice?.message
          ?? ''
        }
        confirmText="확인"
        showCancel={false}
        onCancel={() =>
          setNotice(null)
        }
        onConfirm={() =>
          setNotice(null)
        }
      />

    </main>
  )
}


export default AdminMembersPage