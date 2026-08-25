import {
  useEffect,
  useRef,
  useState,
} from 'react'
import { useNavigate } from 'react-router-dom'

import {
  getCurrentUser,
  logout,
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

        // ★ 여기부터 추가
        const hasRealEmail =
          Boolean(user.email)
          && !user.email.endsWith('@kakao.local')

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
          navigate('/', {
            replace: true,
          })

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
        birthDate: form.birthDate,
        phone:
          `010${form.phoneMiddle}${form.phoneLast}`,
        smsAgreed: form.smsAgreed,
        emailAgreed: form.emailAgreed,
      })

      /*
       * 저장이 성공하면 추가정보 입력이 완료된 상태이므로
       * 메인 화면으로 이동합니다.
       */
      navigate('/', {
        replace: true,
      })
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
              placeholder="950730 또는 19950730"
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

            {providedFields.email ? (
              <>
                <input
                  id="additionalEmail"
                  type="email"
                  value={form.email}
                  readOnly
                  autoComplete="email"
                />

                <p className="field-guide">
                  소셜 계정에서 확인된 정보입니다.
                </p>
              </>
            ) : (
              <>
                <input
                  id="additionalEmail"
                  type="text"
                  value={
                    form.provider === 'KAKAO'
                      ? '카카오 계정으로 인증됨'
                      : '소셜 계정에서 이메일을 제공하지 않음'
                  }
                  readOnly
                />

                <p className="field-guide">
                  이메일 정보는 추후 회원정보에서 등록할 수 있습니다.
                </p>
              </>
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