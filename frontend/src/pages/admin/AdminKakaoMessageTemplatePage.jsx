import {
  useEffect,
  useMemo,
  useState,
} from 'react'
import { Link } from 'react-router-dom'

import {
  getAdminKakaoMessageTemplate,
  updateAdminKakaoMessageTemplate,
} from '../../api/apiClient.js'

import AdminSectionNav
  from '../../components/admin/AdminSectionNav.jsx'

import './AdminKakaoMessageTemplatePage.css'


const placeholders = [
  '#{예약자명}',
  '#{예약일시}',
  '#{헌혈종류}',
  '#{헌혈장소}',
]

const previewValues = {
  '#{예약자명}': '홍길동',
  '#{예약일시}': '2026-09-13 16:30',
  '#{헌혈종류}': '혈소판',
  '#{헌혈장소}': '헌혈의집 구월센터',
}


function makePreview(messageText) {
  return Object.entries(previewValues)
    .reduce(
      (text, [placeholder, value]) =>
        text.replaceAll(placeholder, value),
      messageText
    )
}


function AdminKakaoMessageTemplatePage() {
  const [enabled, setEnabled] = useState(false)
  const [messageText, setMessageText] = useState('')
  const [maximumLength, setMaximumLength] =
    useState(200)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const previewText = useMemo(
    () => makePreview(messageText),
    [messageText]
  )

  const previewLength =
    Array.from(previewText).length

  const overLimit =
    previewLength > maximumLength


  useEffect(() => {
    async function loadTemplate() {
      try {
        const data =
          await getAdminKakaoMessageTemplate()

        setEnabled(data.enabled)
        setMessageText(data.messageText)
        setMaximumLength(data.maximumLength)
      } catch (requestError) {
        setError(
          requestError.message
          || '카카오 메시지 설정을 불러오지 못했습니다.'
        )
      } finally {
        setLoading(false)
      }
    }

    loadTemplate()
  }, [])


  function insertPlaceholder(placeholder) {
    setMessageText(
      (currentText) => currentText + placeholder
    )
    setSuccess('')
  }


  async function handleSubmit(event) {
    event.preventDefault()

    if (!messageText.trim()) {
      setError('발송 내용을 입력해주세요.')
      return
    }

    if (overLimit) {
      setError(
        `예약정보 적용 후 ${maximumLength}자를 넘을 수 없습니다.`
      )
      return
    }

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      const data =
        await updateAdminKakaoMessageTemplate({
          enabled,
          messageText: messageText.trim(),
        })

      setEnabled(data.enabled)
      setMessageText(data.messageText)
      setMaximumLength(data.maximumLength)
      setSuccess('카카오 예약 메시지 설정을 저장했습니다.')
    } catch (requestError) {
      setError(
        requestError.message
        || '카카오 메시지 설정을 저장하지 못했습니다.'
      )
    } finally {
      setSaving(false)
    }
  }


  if (loading) {
    return (
      <main className="kakao-template-page">
        <p className="kakao-template-status">
          카카오 메시지 설정을 불러오는 중입니다.
        </p>
      </main>
    )
  }


  return (
    <main className="kakao-template-page">
      <AdminSectionNav />

      <section className="kakao-template-header">
        <div>
          <span>KAKAO MESSAGE</span>

          <h1>카카오 예약 알림 관리</h1>

          <p>
            카카오 로그인 사용자가 예약을 완료했을 때
            나와의 채팅으로 전송할 내용을 관리합니다.
          </p>
        </div>

        <Link
          to="/admin"
          className="kakao-template-back"
        >
          관리자 홈
        </Link>
      </section>

      <form
        className="kakao-template-layout"
        onSubmit={handleSubmit}
      >
        <section className="kakao-template-editor">
          <div className="kakao-template-toggle-row">
            <div>
              <strong>예약 완료 메시지</strong>
              <span>
                전체 기능 설정과 이 설정이 모두 켜져야 발송됩니다.
              </span>
            </div>

            <label className="kakao-template-toggle">
              <input
                type="checkbox"
                checked={enabled}
                onChange={(event) => {
                  setEnabled(event.target.checked)
                  setSuccess('')
                }}
              />
              <span aria-hidden="true" />
              {enabled ? '사용' : '중지'}
            </label>
          </div>

          <label
            className="kakao-template-field"
            htmlFor="kakao-message-text"
          >
            <span>발송 내용</span>

            <textarea
              id="kakao-message-text"
              value={messageText}
              maxLength={maximumLength}
              rows="11"
              onChange={(event) => {
                setMessageText(event.target.value)
                setSuccess('')
                setError('')
              }}
              aria-describedby="kakao-message-help"
            />
          </label>

          <div className="kakao-template-placeholders">
            <span>예약정보 삽입</span>

            <div>
              {placeholders.map((placeholder) => (
                <button
                  key={placeholder}
                  type="button"
                  onClick={() =>
                    insertPlaceholder(placeholder)
                  }
                >
                  {placeholder}
                </button>
              ))}
            </div>
          </div>

          <p
            id="kakao-message-help"
            className="kakao-template-help"
          >
            카카오 기본 텍스트 템플릿은 실제 예약정보가
            적용된 결과를 기준으로 최대 {maximumLength}자입니다.
          </p>

          {error && (
            <p
              className="kakao-template-feedback error"
              role="alert"
            >
              {error}
            </p>
          )}

          {success && (
            <p
              className="kakao-template-feedback success"
              role="status"
            >
              {success}
            </p>
          )}

          <button
            type="submit"
            className="kakao-template-save"
            disabled={saving || overLimit}
          >
            {saving ? '저장 중...' : '설정 저장'}
          </button>
        </section>

        <aside className="kakao-template-preview">
          <div className="kakao-preview-heading">
            <div>
              <span>PREVIEW</span>
              <h2>발송 미리보기</h2>
            </div>

            <strong className={overLimit ? 'over' : ''}>
              {previewLength} / {maximumLength}
            </strong>
          </div>

          <div className="kakao-chat-preview">
            <span className="kakao-chat-sender">
              BloodOps
            </span>

            <div className="kakao-chat-bubble">
              <strong>알림톡 도착</strong>
              <p>{previewText}</p>
            </div>
          </div>

          <p className="kakao-preview-note">
            실제 카카오 Developers의 나에게 보내기는 정식
            알림톡과 화면 구성이 다를 수 있습니다.
          </p>
        </aside>
      </form>
    </main>
  )
}


export default AdminKakaoMessageTemplatePage
