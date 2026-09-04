import { useRef, useState } from 'react'
import {
  checkEmail,
  checkLoginId,
  confirmEmailVerificationCode,
  confirmPhoneVerificationCode,
  sendEmailVerificationCode,
  sendPhoneVerificationCode,
  signup,
} from '../../api/apiClient.js'
import '../../styles/signup.css'

const EMAIL_DOMAINS = [
  'naver.com',
  'gmail.com',
  'daum.net',
  'kakao.com',
  'custom',
]

function SignupPage() {
  const phoneLastRef = useRef(null)

  const [form, setForm] = useState({
    loginId: '',
    password: '',
    passwordConfirm: '',
    name: '',
    birthDate: '',
    phoneMiddle: '',
    phoneLast: '',
    emailId: '',
    emailDomain: 'naver.com',
    customEmailDomain: '',
    smsAgreed: false,
    emailAgreed: false,
  })

  const [errors, setErrors] = useState({})
  const [submitErrors, setSubmitErrors] = useState([])
  const [generalError, setGeneralError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [loginIdStatus, setLoginIdStatus] = useState('')
  const [emailStatus, setEmailStatus] = useState('')

  const [phoneVerificationCode, setPhoneVerificationCode] =
    useState('')

  const [phoneVerificationSent, setPhoneVerificationSent] =
    useState(false)

  const [phoneVerified, setPhoneVerified] =
    useState(false)

  const [phoneVerificationMessage, setPhoneVerificationMessage] =
    useState('')

  const [isSendingPhoneCode, setIsSendingPhoneCode] =
    useState(false)

  const [isCheckingPhoneCode, setIsCheckingPhoneCode] =
    useState(false)

  const [emailVerificationCode, setEmailVerificationCode] =
    useState('')

  const [emailVerificationSent, setEmailVerificationSent] =
    useState(false)

  const [emailVerified, setEmailVerified] =
    useState(false)

  const [emailVerificationMessage, setEmailVerificationMessage] =
    useState('')

  const [isSendingEmailCode, setIsSendingEmailCode] =
    useState(false)

  const [isCheckingEmailCode, setIsCheckingEmailCode] =
    useState(false)

  function updateField(name, value) {
    setForm((previous) => ({
      ...previous,
      [name]: value,
    }))

    // 사용자가 내용을 다시 수정하면
    // 이전 회원가입 제출 결과는 더 이상 현재 상태가 아닙니다.
    setSubmitErrors([])
    setGeneralError('')

    setErrors((previous) => {
      const next = {
        ...previous,
        [name]: '',
      }

      if (
        name === 'password'
        || name === 'passwordConfirm'
      ) {
        next.password = ''
        next.passwordConfirm = ''
      }

      if (
        name === 'emailId'
        || name === 'emailDomain'
        || name === 'customEmailDomain'
      ) {
        next.email = ''
        next.emailVerificationCode = ''
      }

      if (
        name === 'phoneMiddle'
        || name === 'phoneLast'
      ) {
        next.phone = ''
        next.phoneVerificationCode = ''
      }

      return next
    })

    if (name === 'loginId') {
      setLoginIdStatus('')
    }

    if (
      name === 'emailId'
      || name === 'emailDomain'
      || name === 'customEmailDomain'
    ) {
      setEmailStatus('')
      setEmailVerificationCode('')
      setEmailVerificationSent(false)
      setEmailVerified(false)
      setEmailVerificationMessage('')
    }

    if (
      name === 'phoneMiddle'
      || name === 'phoneLast'
    ) {
      setPhoneVerificationCode('')
      setPhoneVerificationSent(false)
      setPhoneVerified(false)
      setPhoneVerificationMessage('')
    }
  }

  function getEmail() {
    const domain = form.emailDomain === 'custom'
      ? form.customEmailDomain
      : form.emailDomain

    return `${form.emailId}@${domain}`
  }

  function getPhone() {
    return `010${form.phoneMiddle}${form.phoneLast}`
  }

  function clearPhoneVerificationFeedback() {
    setErrors((previous) => ({
      ...previous,
      phone: '',
      phoneVerificationCode: '',
    }))

    setPhoneVerificationMessage('')
    setSubmitErrors([])
    setGeneralError('')
  }

  function clearEmailVerificationFeedback() {
    setErrors((previous) => ({
      ...previous,
      email: '',
      emailVerificationCode: '',
    }))

    setEmailVerificationMessage('')
    setSubmitErrors([])
    setGeneralError('')
  }

  function getPasswordStrength() {
    const password = form.password

    if (password.length < 8) {
      return {
        level: '',
        label: '',
      }
    }

    let score = 0

    if (password.length >= 8) score += 1
    if (/[a-zA-Z]/.test(password)) score += 1
    if (/\d/.test(password)) score += 1
    if (/[^a-zA-Z0-9]/.test(password)) score += 1

    if (score <= 2) {
      return {
        level: 'weak',
        label: '낮음',
      }
    }

    if (score === 3) {
      return {
        level: 'medium',
        label: '보통',
      }
    }

    return {
      level: 'strong',
      label: '안전',
    }
  }

  async function handleLoginIdCheck() {
    const loginIdPattern = /^[a-z0-9]{5,20}$/

    if (!loginIdPattern.test(form.loginId)) {
      setErrors((previous) => ({
        ...previous,
        loginId: '영문 소문자와 숫자를 사용하여 5~20자로 입력해주세요.',
      }))
      setLoginIdStatus('')
      return
    }

    try {
      const result = await checkLoginId(
        form.loginId
      )

      setLoginIdStatus(
        result.available
          ? 'available'
          : 'duplicate'
      )

      setErrors((previous) => ({
        ...previous,
        loginId: result.available
          ? ''
          : '이미 사용 중인 아이디입니다.',
      }))
    } catch (error) {
      setGeneralError(
        error.message
      )
    }
  }

  async function handleEmailCheck() {
    const email = getEmail()
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!emailPattern.test(email)) {
      setErrors((previous) => ({
        ...previous,
        email: '올바른 이메일 주소를 입력해주세요.',
      }))
      setEmailStatus('')
      return
    }

    try {
      const result = await checkEmail(
        email
      )

      setEmailStatus(
        result.available
          ? 'available'
          : 'duplicate'
      )

      setErrors((previous) => ({
        ...previous,
        email: result.available
          ? ''
          : '이미 가입된 이메일입니다.',
      }))
    } catch (error) {
      setGeneralError(
        error.message
      )
    }
  }

  async function handleEmailCheckWithDomain(domain) {
    const email = `${form.emailId}@${domain}`
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!emailPattern.test(email)) {
      return
    }

    try {
      const result = await checkEmail(email)

      setEmailStatus(
        result.available
          ? 'available'
          : 'duplicate'
      )

      setErrors((previous) => ({
        ...previous,
        email: result.available
          ? ''
          : '이미 가입된 이메일입니다.',
      }))
    } catch (error) {
      setGeneralError(error.message)
    }
  }

  async function handleSendEmailVerificationCode() {
    const email = getEmail()
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!emailPattern.test(email)) {
      setErrors((previous) => ({
        ...previous,
        email: '올바른 이메일 주소를 입력해주세요.',
        emailVerificationCode: '',
      }))

      setEmailVerificationMessage('')
      return
    }

    /*
     * 중복 확인을 통과한 이메일에 대해서만
     * 인증번호를 발송합니다.
     */
    if (emailStatus !== 'available') {
      setErrors((previous) => ({
        ...previous,
        email: '먼저 이메일 중복 확인을 해주세요.',
        emailVerificationCode: '',
      }))

      setEmailVerificationMessage('')
      return
    }

    if (isSendingEmailCode) {
      return
    }

    clearEmailVerificationFeedback()
    setIsSendingEmailCode(true)

    /*
     * 발송 요청 전에는 기존 인증번호를 지우지 않습니다.
     * 실제 새 인증번호 발송에 성공했을 때만 지웁니다.
     */
    setEmailVerified(false)
    setEmailVerificationMessage('')

    setErrors((previous) => ({
      ...previous,
      email: '',
      emailVerificationCode: '',
    }))

    try {
      const response =
        await sendEmailVerificationCode(email)

      /*
       * 새 인증번호가 실제 발송된 경우
       * 이전에 입력하던 인증번호를 초기화합니다.
       */
      setEmailVerificationCode('')
      setEmailVerificationSent(true)

      setEmailVerificationMessage(
        response?.message
        || '인증번호를 발송했습니다.'
      )
    } catch (error) {
      const message =
        error.message
        || '인증번호 발송에 실패했습니다.'

      const isResendCooldown =
        message.includes('초 후 다시 발송')

      /*
       * 재발송 대기시간 오류이면
       * 기존 인증번호와 입력값을 유지합니다.
       *
       * 실제 발송 실패일 때만 상태를 초기화합니다.
       */
      if (!isResendCooldown) {
        setEmailVerificationSent(false)
        setEmailVerificationCode('')
      }

      setEmailVerificationMessage('')

      setErrors((previous) => ({
        ...previous,
        email: message,
        emailVerificationCode: '',
      }))
    } finally {
      setIsSendingEmailCode(false)
    }
  }

  async function handleConfirmEmailVerificationCode() {
    const email = getEmail()

    if (!emailVerificationSent) {
      setErrors((previous) => ({
        ...previous,
        emailVerificationCode:
          '먼저 인증번호를 발송해주세요.',
      }))

      return
    }

    if (!/^\d{6}$/.test(emailVerificationCode)) {
      setErrors((previous) => ({
        ...previous,
        emailVerificationCode:
          '인증번호 6자리를 입력해주세요.',
      }))

      return
    }

    if (isCheckingEmailCode) {
      return
    }

    clearEmailVerificationFeedback()
    setIsCheckingEmailCode(true)

    setEmailVerificationMessage('')


    setErrors((previous) => ({
      ...previous,
      emailVerificationCode: '',
    }))

    try {
      const response =
        await confirmEmailVerificationCode(
          email,
          emailVerificationCode
        )

      setEmailVerified(true)

      setEmailVerificationMessage(
        response?.message
        || '이메일 인증이 완료되었습니다.'
      )

      setErrors((previous) => ({
        ...previous,
        email: '',
        emailVerificationCode: '',
      }))
    } catch (error) {
      setEmailVerified(false)
      setEmailVerificationMessage('')

      setErrors((previous) => ({
        ...previous,
        emailVerificationCode:
          error.message
          || '인증번호 확인에 실패했습니다.',
      }))
    } finally {
      setIsCheckingEmailCode(false)
    }
  }

  async function handleSendPhoneVerificationCode() {
    const phone = getPhone()

    if (!/^010\d{8}$/.test(phone)) {
      setErrors((previous) => ({
        ...previous,
        phone: '휴대전화 번호를 정확하게 입력해주세요.',
        phoneVerificationCode: '',
      }))

      setPhoneVerificationMessage('')
      return
    }

    if (isSendingPhoneCode) {
      return
    }

    clearPhoneVerificationFeedback()
    setIsSendingPhoneCode(true)

    /*
     * 발송 요청 전에는 기존 인증번호를 지우지 않습니다.
     * 실제 새 인증번호 발송에 성공했을 때만 지웁니다.
     */
    setPhoneVerified(false)
    setPhoneVerificationMessage('')

    setErrors((previous) => ({
      ...previous,
      phone: '',
      phoneVerificationCode: '',
    }))

    try {
      const response =
        await sendPhoneVerificationCode(phone)

      /*
       * 새 인증번호가 실제 발송된 경우
       * 이전에 입력하던 인증번호를 초기화합니다.
       */
      setPhoneVerificationCode('')
      setPhoneVerificationSent(true)

      setPhoneVerificationMessage(
        response?.message
        || '인증번호를 발송했습니다.'
      )
    } catch (error) {
      const message =
        error.message
        || '인증번호 발송에 실패했습니다.'

      const isResendCooldown =
        message.includes('초 후 다시 발송')

      /*
       * 재발송 대기시간 오류이면
       * 기존에 받은 인증번호를 계속 사용할 수 있으므로
       * 기존 입력값과 발송 상태를 유지합니다.
       *
       * 실제 발송 실패일 때만 상태를 초기화합니다.
       */
      if (!isResendCooldown) {
        setPhoneVerificationSent(false)
        setPhoneVerificationCode('')
      }

      setPhoneVerificationMessage('')

      setErrors((previous) => ({
        ...previous,
        phone: message,
        phoneVerificationCode: '',
      }))
    } finally {
      setIsSendingPhoneCode(false)
    }
  }

  async function handleConfirmPhoneVerificationCode() {
    const phone = getPhone()

    if (!phoneVerificationSent) {
      setErrors((previous) => ({
        ...previous,
        phoneVerificationCode:
          '먼저 인증번호를 발송해주세요.',
      }))

      return
    }

    if (!/^\d{6}$/.test(phoneVerificationCode)) {
      setErrors((previous) => ({
        ...previous,
        phoneVerificationCode:
          '인증번호 6자리를 입력해주세요.',
      }))

      return
    }

    if (isCheckingPhoneCode) {
      return
    }

    clearPhoneVerificationFeedback()
    setIsCheckingPhoneCode(true)

    setPhoneVerificationMessage('')

    setErrors((previous) => ({
      ...previous,
      phoneVerificationCode: '',
    }))

    try {
      const response =
        await confirmPhoneVerificationCode(
          phone,
          phoneVerificationCode
        )

      setPhoneVerified(true)

      setPhoneVerificationMessage(
        response?.message
        || '휴대전화 인증이 완료되었습니다.'
      )

      setErrors((previous) => ({
        ...previous,
        phone: '',
        phoneVerificationCode: '',
      }))
    } catch (error) {
      setPhoneVerified(false)
      setPhoneVerificationMessage('')

      setErrors((previous) => ({
        ...previous,
        phoneVerificationCode:
          error.message
          || '인증번호 확인에 실패했습니다.',
      }))
    } finally {
      setIsCheckingPhoneCode(false)
    }
  }

  function validateForm() {
    const nextErrors = {}

    if (!/^[a-z0-9]{5,20}$/.test(form.loginId)) {
      nextErrors.loginId =
        '아이디는 영문 소문자와 숫자를 사용하여 5~20자로 입력해주세요.'
    } else if (loginIdStatus !== 'available') {
      nextErrors.loginId = '아이디 중복 확인을 해주세요.'
    }

    if (form.password.length < 8 || form.password.length > 16) {
      nextErrors.password =
        '비밀번호는 8자 이상, 16자 이하로 입력해주세요.'
    }

    if (form.password !== form.passwordConfirm) {
      nextErrors.passwordConfirm =
        '비밀번호와 비밀번호 확인이 일치하지 않습니다.'
    }

    if (!form.name.trim()) {
      nextErrors.name = '성명을 입력해주세요.'
    }

    if (!/^(\d{6}|\d{8})$/.test(form.birthDate)) {
      nextErrors.birthDate =
        '생년월일은 숫자 6자리 또는 8자리로 입력해주세요.'
    } else {
      let normalizedBirthDate =
        form.birthDate

      if (form.birthDate.length === 6) {
        const year =
          Number(
            form.birthDate.slice(0, 2)
          )

        const currentYear =
          new Date().getFullYear() % 100

        const century =
          year <= currentYear
            ? '20'
            : '19'

        normalizedBirthDate =
          `${century}${form.birthDate}`
      }

      const year =
        Number(normalizedBirthDate.slice(0, 4))

      const month =
        Number(normalizedBirthDate.slice(4, 6))

      const day =
        Number(normalizedBirthDate.slice(6, 8))

      const birthDate =
        new Date(
          year,
          month - 1,
          day
        )

      const today =
        new Date()

      today.setHours(
        0,
        0,
        0,
        0
      )

      const isValidDate =
        birthDate.getFullYear() === year
        && birthDate.getMonth()
          === month - 1
        && birthDate.getDate() === day

      if (!isValidDate) {
        nextErrors.birthDate =
          '올바른 생년월일을 입력해주세요.'
      } else if (birthDate > today) {
        nextErrors.birthDate =
          '미래 날짜는 생년월일로 사용할 수 없습니다.'
      }
    }

    if (
      !/^\d{4}$/.test(form.phoneMiddle)
      || !/^\d{4}$/.test(form.phoneLast)
    ) {
      nextErrors.phone =
        '휴대전화 번호를 정확하게 입력해주세요.'
    } else if (!phoneVerified) {
      nextErrors.phone =
        '휴대전화 인증을 완료해주세요.'
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(getEmail())) {
      nextErrors.email =
        '올바른 이메일 주소를 입력해주세요.'
    } else if (emailStatus !== 'available') {
      nextErrors.email =
        '이메일 사용 가능 여부를 확인해주세요.'
    } else if (!emailVerified) {
      nextErrors.email =
        '이메일 인증을 완료해주세요.'
    }

    return nextErrors
  }

  async function handleSubmit(event) {
    event.preventDefault()

    setGeneralError('')

    const nextErrors = validateForm()
    const messages = [
      ...new Set(
        Object.values(nextErrors).filter(Boolean)
      ),
    ]

    setErrors(nextErrors)
    setSubmitErrors(messages)

    if (messages.length > 0) {
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
      return
    }

    const memberData = {
      loginId: form.loginId,
      password: form.password,
      passwordConfirm: form.passwordConfirm,
      name: form.name.trim(),
      birthDate: form.birthDate,
      phone: getPhone(),
      email: getEmail(),
      smsAgreed: form.smsAgreed,
      emailAgreed: form.emailAgreed,
    }

    setIsSubmitting(true)

    try {
      await signup(memberData)

      alert('회원가입이 완료되었습니다.')
      window.location.href = '/login'
    } catch (error) {
      const fieldErrors =
        error.fieldErrors ?? {}

      setErrors(fieldErrors)

      setSubmitErrors([
        ...new Set(
          Object.values(fieldErrors)
            .filter(Boolean)
        ),
      ])

      setGeneralError(
        error.message || '회원가입에 실패했습니다.'
      )

      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })

      setIsSubmitting(false)
    }
  }

  const passwordStrength = getPasswordStrength()

  return (
    <main className="signup-page">
      <section className="signup-card">
        <header className="signup-heading">
          <p className="signup-eyebrow">BloodOps Account</p>
          <h1>회원가입</h1>
          <p>헌혈 예약과 개인 기록 관리를 위한 계정을 만듭니다.</p>
        </header>

        {/* 브라우저 기본 오류 팝업을 사용하지 않습니다. */}
        <form onSubmit={handleSubmit} noValidate>
          {(generalError || submitErrors.length > 0) && (
            <div className="error-summary" role="alert">
              <strong>입력 내용을 확인해주세요.</strong>

              {generalError && (
                <p>{generalError}</p>
              )}

              {submitErrors.length > 0 && (
                <ul>
                  {submitErrors.map((message) => (
                    <li key={message}>
                      {message}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          <div className="signup-grid">
            <div className="form-field form-field-wide">
              <label htmlFor="loginId">아이디</label>

              <div className="field-with-button">
                <input
                  id="loginId"
                  type="text"
                  value={form.loginId}
                  placeholder="영문 소문자·숫자 5~20자"
                  autoComplete="username"
                  onChange={(event) => {
                    const value = event.target.value
                      .toLowerCase()
                      .replace(/[^a-z0-9]/g, '')

                    updateField('loginId', value)
                  }}
                />

                <button
                  type="button"
                  onClick={handleLoginIdCheck}
                >
                  중복 확인
                </button>
              </div>

              {errors.loginId ? (
                <p className="field-error">
                  {errors.loginId}
                </p>
              ) : loginIdStatus === 'available' ? (
                <p className="field-success">
                  사용할 수 있는 아이디입니다.
                </p>
              ) : (
                <p className="field-guide">
                  영문 소문자와 숫자를 사용하여 5~20자로 입력해주세요.
                </p>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="password">비밀번호</label>
              <input
                id="password"
                type="password"
                value={form.password}
                placeholder="8~16자"
                autoComplete="new-password"
                onChange={(event) =>
                  updateField('password', event.target.value)
                }
              />

              {passwordStrength.label && (
                <div className={`password-strength ${passwordStrength.level}`}>
                  <span>비밀번호 안전도</span>
                  <strong>{passwordStrength.label}</strong>
                </div>
              )}

              {errors.password && (
                <p className="field-error">{errors.password}</p>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="passwordConfirm">비밀번호 확인</label>
              <input
                id="passwordConfirm"
                type="password"
                value={form.passwordConfirm}
                placeholder="비밀번호를 다시 입력해주세요."
                autoComplete="new-password"
                onChange={(event) =>
                  updateField('passwordConfirm', event.target.value)
                }
              />

              {form.passwordConfirm && (
                <p
                  className={
                    form.password === form.passwordConfirm
                      ? 'field-success'
                      : 'field-error'
                  }
                >
                  {form.password === form.passwordConfirm
                    ? '비밀번호가 일치합니다.'
                    : '비밀번호가 일치하지 않습니다.'}
                </p>
              )}

              {errors.passwordConfirm
                && !form.passwordConfirm && (
                <p className="field-error">
                  {errors.passwordConfirm}
                </p>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="name">성명</label>
              <input
                id="name"
                type="text"
                value={form.name}
                placeholder="성명을 입력해주세요."
                autoComplete="name"
                onChange={(event) =>
                  updateField('name', event.target.value)
                }
              />

              {errors.name && (
                <p className="field-error">{errors.name}</p>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="birthDate">생년월일</label>

              <div className="birth-date-fields">
                <input
                  id="birthDate"
                  type="text"
                  inputMode="numeric"
                  maxLength={8}
                  value={form.birthDate}
                  placeholder="450815 또는 19450815"
                  onChange={(event) => {
                    const value = event.target.value.replace(/\D/g, '')
                    updateField('birthDate', value)
                  }}
                />

                <input
                  type="date"
                  aria-label="달력에서 생년월일 선택"
                  max={
                    new Date()
                      .toISOString()
                      .slice(0, 10)
                  }
                  onChange={(event) => {
                    const value =
                      event.target.value.replaceAll('-', '')

                    updateField(
                      'birthDate',
                      value
                    )
                  }}
                />
              </div>

              {errors.birthDate && (
                <p className="field-error">{errors.birthDate}</p>
              )}
            </div>

            <div className="form-field form-field-wide">
              <label>휴대전화</label>

              <div className="phone-fields">
                <select
                  aria-label="휴대전화 앞 번호"
                  value="010"
                  disabled
                >
                  <option value="010">010</option>
                </select>

                <span>-</span>

                <input
                  type="text"
                  inputMode="numeric"
                  maxLength={4}
                  value={form.phoneMiddle}
                  aria-label="휴대전화 가운데 번호"
                  disabled={phoneVerified}
                  onChange={(event) => {
                    const value =
                      event.target.value.replace(/\D/g, '')

                    updateField('phoneMiddle', value)

                    if (value.length === 4) {
                      phoneLastRef.current?.focus()
                    }
                  }}
                />

                <span>-</span>

                <input
                  ref={phoneLastRef}
                  type="text"
                  inputMode="numeric"
                  maxLength={4}
                  value={form.phoneLast}
                  aria-label="휴대전화 마지막 번호"
                  disabled={phoneVerified}
                  onChange={(event) => {
                    const value =
                      event.target.value.replace(/\D/g, '')

                    updateField('phoneLast', value)
                  }}
                />

                <button
                  type="button"
                  disabled={
                    isSendingPhoneCode
                    || phoneVerified
                  }
                  onClick={handleSendPhoneVerificationCode}
                >
                  {isSendingPhoneCode
                    ? '발송 중...'
                    : phoneVerified
                      ? '인증 완료'
                      : phoneVerificationSent
                        ? '재발송'
                        : '인증번호 발송'}
                </button>
              </div>

            {phoneVerificationSent && (
              <div className="verification-row">
                <div className="verification-code-input">
                  <input
                    type="text"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    maxLength={6}
                    value={phoneVerificationCode}
                    placeholder={
                      phoneVerificationSent
                        ? '인증번호 6자리'
                        : '인증번호 발송 후 입력'
                    }
                    aria-label="휴대전화 인증번호"
                    disabled={
                      !phoneVerificationSent
                      || phoneVerified
                    }
                    onChange={(event) => {
                      const value =
                        event.target.value
                          .replace(/\D/g, '')
                          .slice(0, 6)

                      setPhoneVerificationCode(value)
                      clearPhoneVerificationFeedback()

                      setErrors((previous) => ({
                        ...previous,
                        phone: '',
                        phoneVerificationCode: '',
                      }))

                      setPhoneVerificationMessage('')
                    }}
                  />
                </div>

                <button
                  type="button"
                  disabled={
                    !phoneVerificationSent
                    || phoneVerified
                    || isCheckingPhoneCode
                    || phoneVerificationCode.length !== 6
                  }
                  onClick={handleConfirmPhoneVerificationCode}
                >
                  {phoneVerified
                    ? '인증 완료'
                    : isCheckingPhoneCode
                      ? '확인 중...'
                      : '인증하기'}
                </button>
              </div>
              )}

              {errors.phone && (
                <p className="field-error">
                  {errors.phone}
                </p>
              )}

              {errors.phoneVerificationCode && (
                <p className="field-error">
                  {errors.phoneVerificationCode}
                </p>
              )}

              {phoneVerificationMessage && (
                <p
                  className={
                    phoneVerified
                      ? 'field-success'
                      : 'field-guide'
                  }
                >
                  {phoneVerified ? '✓ ' : ''}
                  {phoneVerificationMessage}
                </p>
              )}
            </div>

            <div className="form-field form-field-wide">
              <label>이메일</label>

              <div className="email-fields">
                <input
                  type="text"
                  value={form.emailId}
                  aria-label="이메일 아이디"
                  placeholder="이메일 아이디"
                  disabled={emailVerified}
                  onChange={(event) => {
                    const value = event.target.value
                      .replace(/[^a-zA-Z0-9._+-]/g, '')

                    updateField(
                      'emailId',
                      value
                    )
                  }}
                    onBlur={handleEmailCheck}
                />

                <span>@</span>

                {form.emailDomain === 'custom' ? (
                  <input
                    type="text"
                    value={form.customEmailDomain}
                    aria-label="이메일 도메인 직접 입력"
                    placeholder="도메인 직접 입력"
                    disabled={emailVerified}
                    onChange={(event) => {
                      const value = event.target.value
                        .toLowerCase()
                        .replace(/[^a-z0-9.-]/g, '')

                      updateField(
                        'customEmailDomain',
                        value
                      )
                    }}
                    onBlur={handleEmailCheck}
                  />
                ) : (
                  <select
                    value={form.emailDomain}
                    aria-label="이메일 도메인 선택"
                    disabled={emailVerified}
                    onChange={(event) => {
                      const nextDomain = event.target.value

                      updateField(
                        'emailDomain',
                        nextDomain
                      )

                      if (nextDomain !== 'custom' && form.emailId) {
                        handleEmailCheckWithDomain(nextDomain)
                      }
                    }}
                  >
                    {EMAIL_DOMAINS.map((domain) => (
                      <option
                        key={domain}
                        value={domain}
                      >
                        {domain === 'custom'
                          ? '직접 입력'
                          : domain}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              {emailStatus === 'available'
                && !emailVerified && (
                  <p className="field-success">
                    사용할 수 있는 이메일입니다.
                  </p>
              )}

            {!emailVerificationSent
              && !emailVerified && (
              <button
                type="button"
                disabled={
                  isSendingEmailCode
                  || emailStatus !== 'available'
                }
                onClick={handleSendEmailVerificationCode}
              >
                {isSendingEmailCode
                  ? '발송 중...'
                  : '인증번호 발송'}
              </button>
            )}

            {emailVerificationSent && (
              <div className="verification-row">
                <div className="verification-code-input">
                  <input
                    type="text"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    maxLength={6}
                    value={emailVerificationCode}
                    placeholder={
                      emailVerificationSent
                        ? '인증번호 6자리'
                        : '인증번호 발송 후 입력'
                    }
                    aria-label="이메일 인증번호"
                    disabled={
                      !emailVerificationSent
                      || emailVerified
                    }
                    onChange={(event) => {
                      const value =
                        event.target.value
                          .replace(/\D/g, '')
                          .slice(0, 6)

                      setEmailVerificationCode(value)
                      clearEmailVerificationFeedback()

                      setErrors((previous) => ({
                        ...previous,
                        email: '',
                        emailVerificationCode: '',
                      }))

                      setEmailVerificationMessage('')
                    }}
                  />
                </div>

                <button
                  type="button"
                  disabled={
                    isSendingEmailCode
                    || emailVerified
                    || emailStatus !== 'available'
                  }
                  onClick={handleSendEmailVerificationCode}
                >
                  {isSendingEmailCode
                    ? '발송 중...'
                    : emailVerificationSent
                      ? '재발송'
                      : '인증번호 발송'}
                </button>

                <button
                  type="button"
                  disabled={
                    !emailVerificationSent
                    || emailVerified
                    || isCheckingEmailCode
                    || emailVerificationCode.length !== 6
                  }
                  onClick={
                    handleConfirmEmailVerificationCode
                  }
                >
                  {emailVerified
                    ? '인증 완료'
                    : isCheckingEmailCode
                      ? '확인 중...'
                      : '인증하기'}
                </button>
              </div>
              )}

              {errors.email && (
                <p className="field-error">
                  {errors.email}
                </p>
              )}

              {errors.emailVerificationCode && (
                <p className="field-error">
                  {errors.emailVerificationCode}
                </p>
              )}

              {emailVerificationMessage && (
                <p
                  className={
                    emailVerified
                      ? 'field-success'
                      : 'field-guide'
                  }
                >
                  {emailVerified ? '✓ ' : ''}
                  {emailVerificationMessage}
                </p>
              )}
            </div>
          </div>

          <fieldset className="receive-options">
            <legend>선택 수신 동의</legend>

            <label>
              <input
                type="checkbox"
                checked={form.smsAgreed}
                onChange={(event) =>
                  updateField('smsAgreed', event.target.checked)
                }
              />
              SMS로 헌혈 일정 및 소식을 받습니다.
            </label>

            <label>
              <input
                type="checkbox"
                checked={form.emailAgreed}
                onChange={(event) =>
                  updateField('emailAgreed', event.target.checked)
                }
              />
              이메일로 BloodOps 소식을 받습니다.
            </label>
          </fieldset>

          <div className="signup-actions">
            <button
              type="button"
              onClick={() => {
                window.location.href = '/login'
              }}
            >
              취소
            </button>

            <button
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? '가입 처리 중...' : '회원가입'}
            </button>
          </div>
        </form>
      </section>
    </main>
  )
}

export default SignupPage