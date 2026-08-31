import {
  useEffect,
  useRef,
  useState,
} from 'react'
import { useNavigate } from 'react-router-dom'

import {
  confirmEmailVerificationCode,
  getCurrentUser,
  getOAuthReturnTo,
  logout,
  sendEmailVerificationCode,
  updateAdditionalInfo,
} from '../../api/apiClient'

import '../../styles/signup.css'


function AdditionalInfoPage() {
  const phoneLastRef =
    useRef(null)

  const navigate = useNavigate()

  const [form, setForm] = useState({
      provider: '',
      name: '',
      email: '',
      birthDate: '',
      phoneMiddle: '',
      phoneLast: '',
      smsAgreed: false,
      emailAgreed: false,
    })

    const [providedFields, setProvidedFields] =
      useState({
        name: false,
        email: false,
        birthDate: false,
        phone: false,
      })

  const [errors, setErrors] =
    useState({})

  const [isLoading, setIsLoading] =
    useState(true)

  const [isSubmitting, setIsSubmitting] =
    useState(false)

  const [verificationCode, setVerificationCode] =
    useState('')

  const [isSendingCode, setIsSendingCode] =
    useState(false)

  const [isCheckingCode, setIsCheckingCode] =
    useState(false)

  const [emailVerified, setEmailVerified] =
    useState(false)

  const [
    verificationMessage,
    setVerificationMessage,
  ] = useState('')

  const [
    verificationExpiresIn,
    setVerificationExpiresIn,
  ] = useState(0)

  const [
    resendAvailableIn,
    setResendAvailableIn,
  ] = useState(0)

  const [
    verificationSent,
    setVerificationSent,
  ] = useState(false)

useEffect(() => {
  if (verificationExpiresIn <= 0) {
    return undefined
  }

  const timer = window.setInterval(() => {
    setVerificationExpiresIn(
      (current) =>
        Math.max(
          current - 1,
          0
        )
    )
  }, 1000)

  return () => {
    window.clearInterval(timer)
  }
}, [verificationExpiresIn])

useEffect(() => {
  if (resendAvailableIn <= 0) {
    return undefined
  }

  const timer = window.setInterval(() => {
    setResendAvailableIn(
      (current) =>
        Math.max(
          current - 1,
          0
        )
    )
  }, 1000)

  return () => {
    window.clearInterval(timer)
  }
}, [resendAvailableIn])

useEffect(() => {
  if (
    verificationSent
    && !emailVerified
    && verificationExpiresIn === 0
  ) {
    setVerificationMessage(
      '인증번호가 만료되었습니다. 인증번호를 다시 발송해주세요.'
    )
  }
}, [
  verificationExpiresIn,
  verificationSent,
  emailVerified,
])


  /*
   * 현재 로그인 회원이 실제로
   * 추가정보 입력 대상인지 확인합니다.
   */
  useEffect(() => {
    let isMounted = true

    async function checkUser() {
      try {
        const user =
          await getCurrentUser()

        if (!isMounted) {
          return
        }

        const hasRealEmail =
                  Boolean(user.email)

        setForm({
          provider: user.provider ?? '',
          name: user.name ?? '',
          email: hasRealEmail
            ? user.email
            : '',
          birthDate: user.birthDate ?? '',
          phoneMiddle:
            user.phone
              ? user.phone
                  .replace(/\D/g, '')
                  .slice(3, 7)
              : '',

          phoneLast:
            user.phone
              ? user.phone
                  .replace(/\D/g, '')
                  .slice(7, 11)
              : '',
          smsAgreed: user.smsAgreed ?? false,
          emailAgreed: user.emailAgreed ?? false,
        })

        setProvidedFields({
          name: Boolean(user.name),
          email: hasRealEmail,
          birthDate: Boolean(user.birthDate),
          phone: Boolean(user.phone),
        })

        /*
         * 이미 생년월일/전화번호가 있는 회원이
         * 주소를 직접 입력해 접근한 경우
         * 메인 화면으로 돌려보냅니다.
         */
        if (!user.additionalInfoRequired) {
          const returnData =
            await getOAuthReturnTo()

          navigate(
            returnData?.returnTo || '/',
            {
              replace: true,
            }
          )

          return
        }

        setIsLoading(false)
      } catch {
        /*
         * 로그인되지 않은 사용자는
         * 추가정보를 입력할 수 없습니다.
         */
        navigate('/login', {
          replace: true,
        })
      }
    }

    checkUser()

    return () => {
      isMounted = false
    }
  }, [navigate])

  function updateField(name, value) {
    setForm((current) => ({
      ...current,
      [name]: value,
    }))

    setErrors((current) => ({
      ...current,
      [name]: '',
    }))

    if (name === 'email') {
      setEmailVerified(false)

      if (verificationSent) {
        setVerificationMessage(
          '이메일이 변경되었습니다. 새 이메일로 인증번호를 다시 발송해주세요.'
        )
      } else {
        setVerificationMessage('')
      }
    }
  }

  /*
   * 생년월일은 숫자만 입력받습니다.
   * 6자리 또는 8자리 검증은 서버에서 수행합니다.
   */
  function handleBirthDateChange(event) {
      const value =
        event.target.value
          .replace(/\D/g, '')
          .slice(0, 8)

      updateField(
        'birthDate',
        value
      )
    }

  function validateBirthDate(
    birthDate
  ) {
    if (
      !/^(\d{6}|\d{8})$/.test(
        birthDate
      )
    ) {
      return (
        '생년월일은 숫자 6자리 또는 8자리로 입력해주세요.'
      )
    }

    let normalizedBirthDate =
      birthDate

    if (birthDate.length === 6) {
      const inputYear =
        Number(
          birthDate.slice(0, 2)
        )

      const currentYear =
        new Date().getFullYear()
        % 100

      const century =
        inputYear <= currentYear
          ? '20'
          : '19'

      normalizedBirthDate =
        century + birthDate
    }

    const year =
      Number(
        normalizedBirthDate.slice(
          0,
          4
        )
      )

    const month =
      Number(
        normalizedBirthDate.slice(
          4,
          6
        )
      )

    const day =
      Number(
        normalizedBirthDate.slice(
          6,
          8
        )
      )

    const birthDateValue =
      new Date(
        year,
        month - 1,
        day
      )

    if (
      birthDateValue.getFullYear()
        !== year
      || birthDateValue.getMonth()
        !== month - 1
      || birthDateValue.getDate()
        !== day
    ) {
      return (
        '존재하지 않는 생년월일입니다.'
      )
    }

    const today =
      new Date()

    today.setHours(
      0,
      0,
      0,
      0
    )

    birthDateValue.setHours(
      0,
      0,
      0,
      0
    )

    if (
      birthDateValue > today
    ) {
      return (
        '생년월일은 오늘 이후 날짜로 입력할 수 없습니다.'
      )
    }

    return ''
  }

function formatVerificationTime(
  totalSeconds
) {
  const minutes =
    Math.floor(
      totalSeconds / 60
    )

  const seconds =
    totalSeconds % 60

  return `${String(minutes).padStart(
    2,
    '0'
  )}:${String(seconds).padStart(
    2,
    '0'
  )}`
}

async function handleSendVerificationCode() {
  const email =
    form.email.trim()

  if (!email) {
    setErrors((current) => ({
      ...current,
      email:
        '이메일을 입력해주세요.',
    }))

    return
  }

  if (
    !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
      email
    )
  ) {
    setErrors((current) => ({
      ...current,
      email:
        '올바른 이메일 형식으로 입력해주세요.',
    }))

    return
  }

  if (isSendingCode) {
    return
  }

  setIsSendingCode(true)
  setVerificationMessage('')

  try {
    const response =
      await sendEmailVerificationCode(
        email
      )

    setEmailVerified(false)
    setVerificationCode('')
    setVerificationSent(true)

    setVerificationExpiresIn(
      response?.expiresInSeconds
      ?? 300
    )

    setResendAvailableIn(
      response?.resendAvailableInSeconds
      ?? 120
    )

    setVerificationMessage(
      response?.message
      || '인증번호를 발송했습니다.'
    )

    setErrors((current) => ({
      ...current,
      email: '',
      verificationCode: '',
    }))
  } catch (error) {
    setErrors((current) => ({
      ...current,
      email:
        error.message
        || '인증번호 발송에 실패했습니다.',
    }))
  } finally {
    setIsSendingCode(false)
  }
}


async function handleConfirmVerificationCode() {
  const email =
    form.email.trim()

  if (
    !/^\d{6}$/.test(
      verificationCode
    )
  ) {
    setErrors((current) => ({
      ...current,
      verificationCode:
        '인증번호 6자리를 입력해주세요.',
    }))

    return
  }

  if (isCheckingCode) {
    return
  }

  setIsCheckingCode(true)

  try {
    const response =
      await confirmEmailVerificationCode(
        email,
        verificationCode
      )

    setEmailVerified(true)
    setVerificationExpiresIn(0)

    setVerificationMessage(
      response?.message
      || '이메일 인증이 완료되었습니다.'
    )

    setErrors((current) => ({
      ...current,
      email: '',
      verificationCode: '',
    }))
  } catch (error) {
    setEmailVerified(false)

    setErrors((current) => ({
      ...current,
      verificationCode:
        error.message
        || '인증번호 확인에 실패했습니다.',
    }))
  } finally {
    setIsCheckingCode(false)
  }
}

  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate(
        '/login',
        {
          replace: true,
        }
      )
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    setErrors({})

    if (!emailVerified) {
      setErrors({
        email:
          '이메일 인증을 완료해주세요.',
      })

      return
    }

    if (
      !providedFields.birthDate
    ) {
      const birthDateError =
        validateBirthDate(
          form.birthDate
        )

      if (birthDateError) {
        setErrors({
          birthDate:
            birthDateError,
        })

        return
      }
    }

    if (
      !providedFields.phone
      && (
        !/^\d{4}$/.test(form.phoneMiddle)
        || !/^\d{4}$/.test(form.phoneLast)
      )
    ) {
      setErrors({
        phone:
          '휴대전화 번호를 정확하게 입력해주세요.',
      })

      return
    }

    setIsSubmitting(true)

    try {
      await updateAdditionalInfo({
        email: form.email.trim(),
        birthDate: form.birthDate,
        phone:
          `010${form.phoneMiddle}${form.phoneLast}`,
        smsAgreed: form.smsAgreed,
        emailAgreed: form.emailAgreed,
      })

      const returnData =
        await getOAuthReturnTo()

      const returnTo =
        returnData?.returnTo || '/'

      navigate(
        returnTo,
        {
          replace: true,
        }
      )
    } catch (error) {
      /*
       * apiRequest()가 서버의 errors 객체를
       * error.fieldErrors에 저장하고 있으므로
       * 해당 값을 사용합니다.
       */
      const fieldErrors =
        error.fieldErrors ?? {}

      if (
        Object.keys(fieldErrors).length > 0
      ) {
        setErrors(fieldErrors)
      } else {
        setErrors({
          form:
            error.message
            || '추가 정보 저장에 실패했습니다.',
        })
      }
    } finally {
      setIsSubmitting(false)
    }
  }


  if (isLoading) {
    return (
      <main className="signup-page">
        <section className="signup-card">
          <p className="additional-info-loading">
            회원 정보를 확인하고 있습니다.
          </p>
        </section>
      </main>
    )
  }


  return (
    <main className="signup-page">
      <section className="signup-card">

      <div className="signup-heading">
        <p className="signup-eyebrow">
          BLOODOPS ACCOUNT
        </p>

        <h1>
          회원 정보 입력
        </h1>

        <p>
          {form.provider
            ? `${form.provider} 계정에서 확인된 정보가 자동으로 입력되었습니다. `
            : ''}
          BloodOps 서비스 이용을 위해
          입력되지 않은 추가 정보를 작성해주세요.
        </p>
      </div>


      <form
        onSubmit={handleSubmit}
        noValidate
      >
        <div className="signup-grid">

          <div className="form-field">
            <label htmlFor="additionalName">
              성명
            </label>

            <input
              id="additionalName"
              type="text"
              value={form.name}
              readOnly={providedFields.name}
              placeholder="성명을 입력해주세요."
              autoComplete="name"
              onChange={(event) =>
                updateField(
                  'name',
                  event.target.value
                )
              }
            />

            {providedFields.name && (
              <p className="field-guide">
                소셜 계정에서 확인된 정보입니다.
              </p>
            )}
          </div>


          <div className="form-field">
            <label htmlFor="additionalBirthDate">
              생년월일
            </label>

            <input
              id="additionalBirthDate"
              type="text"
              inputMode="numeric"
              maxLength={8}
              value={form.birthDate}
              readOnly={providedFields.birthDate}
              placeholder="450815 또는 19450815"
              onChange={handleBirthDateChange}
            />

            {providedFields.birthDate && (
              <p className="field-guide">
                소셜 계정에서 확인된 정보입니다.
              </p>
            )}

            {errors.birthDate && (
              <p className="field-error">
                {errors.birthDate}
              </p>
            )}
          </div>


          <div className="form-field form-field-wide">
            <label>
              휴대전화
            </label>

            <div className="phone-fields">
              <select
                aria-label="휴대전화 앞 번호"
                value="010"
                disabled
              >
                <option value="010">
                  010
                </option>
              </select>

              <span>-</span>

              <input
                type="text"
                inputMode="numeric"
                maxLength={4}
                value={form.phoneMiddle}
                readOnly={providedFields.phone}
                aria-label="휴대전화 가운데 번호"
                onChange={(event) => {
                  const value =
                    event.target.value
                      .replace(/\D/g, '')

                  updateField(
                    'phoneMiddle',
                    value
                  )

                  if (
                    value.length === 4
                    && !providedFields.phone
                  ) {
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
                readOnly={providedFields.phone}
                aria-label="휴대전화 마지막 번호"
                onChange={(event) => {
                  const value =
                    event.target.value
                      .replace(/\D/g, '')

                  updateField(
                    'phoneLast',
                    value
                  )
                }}
              />
            </div>

            {providedFields.phone && (
              <p className="field-guide">
                소셜 계정에서 확인된 정보입니다.
              </p>
            )}

            {errors.phone && (
              <p className="field-error">
                {errors.phone}
              </p>
            )}
          </div>


            <div className="form-field form-field-wide">
              <label htmlFor="additionalEmail">
                이메일
              </label>

              <div className="field-with-button">
                <input
                  id="additionalEmail"
                  type="email"
                  value={form.email}
                  readOnly={providedFields.email}
                  placeholder="example@email.com"
                  autoComplete="email"
                  onChange={(event) =>
                    updateField(
                      'email',
                      event.target.value
                    )
                  }
                />

                <button
                  type="button"
                  disabled={
                    isSendingCode
                    || emailVerified
                    || resendAvailableIn > 0
                  }
                  onClick={
                    handleSendVerificationCode
                  }
                >
                  {isSendingCode
                    ? '발송 중...'
                    : emailVerified
                      ? '인증 완료'
                      : resendAvailableIn > 0
                        ? `재발송 ${formatVerificationTime(
                            resendAvailableIn
                          )}`
                        : verificationSent
                          ? '인증번호 재발송'
                          : '인증번호 발송'}
                </button>
              </div>

              {providedFields.email ? (
                <p className="field-guide">
                  소셜 계정에서 확인된 이메일입니다.
                  서비스 이용을 위해 이메일 인증을 진행해주세요.
                </p>
              ) : (
                <p className="field-guide">
                  소셜 계정에서 이메일을 제공하지 않아
                  직접 입력 후 인증이 필요합니다.
                </p>
              )}

              {errors.email && (
                <p className="field-error">
                  {errors.email}
                </p>
              )}

              {!emailVerified && (
                <div className="email-verification-row">
                  <div className="verification-code-input">
                    <input
                      type="text"
                      inputMode="numeric"
                      maxLength={6}
                      value={verificationCode}
                      placeholder="인증번호 6자리"
                      aria-label="이메일 인증번호"
                      onChange={(event) => {
                        const value =
                          event.target.value
                            .replace(/\D/g, '')
                            .slice(0, 6)

                        setVerificationCode(
                          value
                        )

                        setErrors((current) => ({
                          ...current,
                          verificationCode: '',
                        }))
                      }}
                    />

                    {verificationSent
                      && !emailVerified && (
                        <span
                          className={
                            verificationExpiresIn > 0
                              ? 'verification-timer'
                              : 'verification-timer expired'
                          }
                        >
                          {verificationExpiresIn > 0
                            ? formatVerificationTime(
                                verificationExpiresIn
                              )
                            : '만료'}
                        </span>
                      )}
                  </div>

                  <button
                    type="button"
                    disabled={
                      isCheckingCode
                      || verificationCode.length !== 6
                    }
                    onClick={
                      handleConfirmVerificationCode
                    }
                  >
                    {isCheckingCode
                      ? '확인 중...'
                      : '인증하기'}
                  </button>
                </div>
              )}

              {errors.verificationCode && (
                <p className="field-error">
                  {errors.verificationCode}
                </p>
              )}

              {verificationMessage && (
                <p
                  className={
                    emailVerified
                      ? 'field-success'
                      : 'field-guide'
                  }
                >
                  {emailVerified
                    ? '✓ '
                    : ''}
                  {verificationMessage}
                </p>
              )}
            </div>

        </div>


        <fieldset className="receive-options">
          <legend>
            선택 수신 동의
          </legend>

          <label>
            <input
              type="checkbox"
              checked={form.smsAgreed}
              onChange={(event) =>
                updateField(
                  'smsAgreed',
                  event.target.checked
                )
              }
            />

            <span>
              SMS로 헌혈 일정 및 소식을 받습니다.
            </span>
          </label>

          <label>
            <input
              type="checkbox"
              checked={form.emailAgreed}
              onChange={(event) =>
                updateField(
                  'emailAgreed',
                  event.target.checked
                )
              }
            />

            <span>
              이메일로 BloodOps 소식을 받습니다.
            </span>
          </label>
        </fieldset>


        {errors.form && (
          <div className="error-summary">
            <strong>
              추가 정보 저장에 실패했습니다.
            </strong>

            <p>
              {errors.form}
            </p>
          </div>
        )}


        <div className="signup-actions">
          <button
            type="button"
            onClick={handleLogout}
          >
            로그아웃
          </button>

          <button
            type="submit"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? '저장 중...'
              : '추가 정보 저장'}
          </button>
        </div>
      </form>

      </section>

    </main>
  )
}


export default AdditionalInfoPage