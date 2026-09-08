import {
  useEffect,
  useState,
} from 'react'

import {
  createDonationTip,
  deleteDonationTip,
  getCurrentUser,
  getDonationTips,
  updateDonationTip,
  uploadDonationTipImage,
} from '../../api/apiClient.js'

import './DonationTipsPage.css'


const categories = [
  {
    value: 'BEFORE',
    label: '헌혈 전 주의사항',
  },
  {
    value: 'AFTER',
    label: '헌혈 후 주의사항',
  },
]


function createEmptyItem(
  order = 1
) {
  return {
    itemOrder: order,
    title: '',
    content: '',
    imagePath: '',
    uploading: false,
  }
}


function createEmptyForm(
  category = 'BEFORE'
) {
  return {
    category,
    title: '',
    items: [
      createEmptyItem(1),
    ],
  }
}


function DonationTipsPage() {
  const [
    activeCategory,
    setActiveCategory,
  ] = useState('BEFORE')

  const [
    tips,
    setTips,
  ] = useState([])

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState('')

  const [
    isAdmin,
    setIsAdmin,
  ] = useState(false)

  const [
    formOpen,
    setFormOpen,
  ] = useState(false)

  const [
    editingId,
    setEditingId,
  ] = useState(null)

  const [
    form,
    setForm,
  ] = useState(
    createEmptyForm()
  )

  const [
    submitting,
    setSubmitting,
  ] = useState(false)


  useEffect(() => {
    checkAdmin()
  }, [])


  useEffect(() => {
    loadTips(
      activeCategory
    )
  }, [activeCategory])


  async function checkAdmin() {
    try {
      const user =
        await getCurrentUser()

      setIsAdmin(
        user?.roles?.includes(
          'ROLE_ADMIN'
        ) === true
      )
    } catch {
      setIsAdmin(false)
    }
  }


  async function loadTips(
    category
  ) {
    try {
      setLoading(true)
      setError('')

      const data =
        await getDonationTips(
          category
        )

      setTips(
        Array.isArray(data)
          ? data
          : []
      )
    } catch (requestError) {
      setError(
        requestError.message
        || '헌혈팁을 불러오지 못했습니다.'
      )
    } finally {
      setLoading(false)
    }
  }


  function changeCategory(
    category
  ) {
    setActiveCategory(
      category
    )

    setFormOpen(false)
    setEditingId(null)
    setError('')
  }


  function openCreateForm() {
    setEditingId(null)

    setForm(
      createEmptyForm(
        activeCategory
      )
    )

    setError('')
    setFormOpen(true)
  }


  function openEditForm(
    tip
  ) {
    setEditingId(
      tip.id
    )

    setForm({
      category:
        tip.category,

      title:
        tip.title,

      items:
        tip.items.map(
          (item) => ({
            itemOrder:
              item.itemOrder,

            title:
              item.title || '',

            content:
              item.content || '',

            imagePath:
              item.imagePath || '',

            uploading: false,
          })
        ),
    })

    setError('')
    setFormOpen(true)

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }


  function closeForm() {
    if (submitting) {
      return
    }

    setEditingId(null)
    setFormOpen(false)
    setError('')
  }


  function changeFormTitle(
    event
  ) {
    setForm(
      (current) => ({
        ...current,
        title:
          event.target.value,
      })
    )
  }


  function changeFormCategory(
    event
  ) {
    setForm(
      (current) => ({
        ...current,
        category:
          event.target.value,
      })
    )
  }


  function changeItem(
    index,
    field,
    value
  ) {
    setForm(
      (current) => ({
        ...current,

        items:
          current.items.map(
            (item, itemIndex) =>
              itemIndex === index
                ? {
                    ...item,
                    [field]: value,
                  }
                : item
          ),
      })
    )
  }


  function addItem() {
    setForm(
      (current) => ({
        ...current,

        items: [
          ...current.items,

          createEmptyItem(
            current.items.length
              + 1
          ),
        ],
      })
    )
  }


  function removeItem(
    index
  ) {
    setForm(
      (current) => ({
        ...current,

        items:
          current.items
            .filter(
              (_, itemIndex) =>
                itemIndex !== index
            )
            .map(
              (item, itemIndex) => ({
                ...item,

                itemOrder:
                  itemIndex + 1,
              })
            ),
      })
    )
  }


  async function handleImageChange(
    index,
    event
  ) {
    const file =
      event.target.files?.[0]

    event.target.value = ''

    if (!file) {
      return
    }

    try {
      changeItem(
        index,
        'uploading',
        true
      )

      setError('')

      const result =
        await uploadDonationTipImage(
          file
        )

      changeItem(
        index,
        'imagePath',
        result.imagePath
      )
    } catch (requestError) {
      setError(
        requestError.message
        || '이미지를 업로드하지 못했습니다.'
      )
    } finally {
      changeItem(
        index,
        'uploading',
        false
      )
    }
  }


  function removeImage(
    index
  ) {
    changeItem(
      index,
      'imagePath',
      ''
    )
  }


  async function handleSubmit(
    event
  ) {
    event.preventDefault()

    const title =
      form.title.trim()

    if (!title) {
      setError(
        '제목을 입력해주세요.'
      )

      return
    }

    const invalidItem =
      form.items.find(
        (item) =>
          !item.content.trim()
      )

    if (invalidItem) {
      setError(
        `${invalidItem.itemOrder}번 내용을 입력해주세요.`
      )

      return
    }

    if (
      form.items.some(
        (item) =>
          item.uploading
      )
    ) {
      setError(
        '이미지 업로드가 끝난 후 저장해주세요.'
      )

      return
    }

    const requestData = {
      category:
        form.category,

      title,

      items:
        form.items.map(
          (item, index) => ({
            itemOrder:
              index + 1,

            title:
              item.title.trim()
              || null,

            content:
              item.content.trim(),

            imagePath:
              item.imagePath
              || null,
          })
        ),
    }

    try {
      setSubmitting(true)
      setError('')

      if (editingId) {
        await updateDonationTip(
          editingId,
          requestData
        )
      } else {
        await createDonationTip(
          requestData
        )
      }

      const savedCategory =
        form.category

      setEditingId(null)
      setFormOpen(false)

      setActiveCategory(
        savedCategory
      )

      await loadTips(
        savedCategory
      )
    } catch (requestError) {
      setError(
        requestError.message
        || '헌혈팁을 저장하지 못했습니다.'
      )
    } finally {
      setSubmitting(false)
    }
  }


  async function handleDelete(
    tipId
  ) {
    if (
      !window.confirm(
        '이 헌혈팁을 삭제하시겠습니까?'
      )
    ) {
      return
    }

    try {
      setError('')

      await deleteDonationTip(
        tipId
      )

      await loadTips(
        activeCategory
      )
    } catch (requestError) {
      setError(
        requestError.message
        || '헌혈팁을 삭제하지 못했습니다.'
      )
    }
  }


  function formatDate(
    value
  ) {
    if (!value) {
      return ''
    }

    return new Intl.DateTimeFormat(
      'ko-KR',
      {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      }
    ).format(
      new Date(value)
    )
  }


  return (
    <main
      className="donation-guide-page"
    >

      <section
        className="donation-guide-header"
      >
        <div>
          <span
            className="donation-guide-eyebrow"
          >
            DONATION GUIDE
          </span>

          <h1>
            헌혈팁
          </h1>

          <p>
            안전한 헌혈을 위해
            헌혈 전·후 주의사항을
            확인해주세요.
          </p>
        </div>


        {isAdmin && (
          <button
            type="button"
            className="donation-guide-write-button"
            onClick={
              openCreateForm
            }
          >
            안내 작성
          </button>
        )}
      </section>


      <nav
        className="donation-guide-tabs"
        aria-label="헌혈팁 분류"
      >
        {categories.map(
          (category) => (
            <button
              key={
                category.value
              }
              type="button"
              className={
                activeCategory
                === category.value
                  ? 'active'
                  : ''
              }
              onClick={() =>
                changeCategory(
                  category.value
                )
              }
            >
              {category.label}
            </button>
          )
        )}
      </nav>


      {error && (
        <p
          className="donation-guide-error"
          role="alert"
        >
          {error}
        </p>
      )}


      {isAdmin
        && formOpen && (
        <section
          className="donation-guide-editor"
        >
          <div
            className="donation-guide-editor-header"
          >
            <div>
              <span>
                ADMIN
              </span>

              <h2>
                {editingId
                  ? '헌혈팁 수정'
                  : '헌혈팁 작성'}
              </h2>
            </div>

            <button
              type="button"
              className="donation-guide-editor-close"
              onClick={
                closeForm
              }
            >
              ×
            </button>
          </div>


          <form
            onSubmit={
              handleSubmit
            }
          >
            <div
              className="donation-guide-editor-basic"
            >
              <label>
                구분

                <select
                  value={
                    form.category
                  }
                  onChange={
                    changeFormCategory
                  }
                >
                  {categories.map(
                    (category) => (
                      <option
                        key={
                          category.value
                        }
                        value={
                          category.value
                        }
                      >
                        {
                          category.label
                        }
                      </option>
                    )
                  )}
                </select>
              </label>


              <label>
                안내 제목

                <input
                  type="text"
                  value={
                    form.title
                  }
                  onChange={
                    changeFormTitle
                  }
                  maxLength={120}
                  placeholder="예: 헌혈 전 몇 가지 유의사항을 알려드립니다."
                />
              </label>
            </div>


            <div
              className="donation-guide-item-editor-list"
            >
              {form.items.map(
                (item, index) => (
                  <article
                    key={index}
                    className="donation-guide-item-editor"
                  >
                    <div
                      className="donation-guide-item-editor-number"
                    >
                      {index + 1}
                    </div>

                    <div
                      className="donation-guide-item-editor-fields"
                    >
                      <label>
                        항목 제목
                        <span>
                          선택
                        </span>

                        <input
                          type="text"
                          value={
                            item.title
                          }
                          onChange={
                            (event) =>
                              changeItem(
                                index,
                                'title',
                                event.target.value
                              )
                          }
                          maxLength={120}
                        />
                      </label>


                      <label>
                        안내 문구

                        <textarea
                          value={
                            item.content
                          }
                          onChange={
                            (event) =>
                              changeItem(
                                index,
                                'content',
                                event.target.value
                              )
                          }
                          maxLength={500}
                          rows={4}
                        />
                      </label>


                      <div
                        className="donation-guide-image-editor"
                      >
                        {item.imagePath ? (
                          <div
                            className="donation-guide-image-preview"
                          >
                            <img
                              src={
                                item.imagePath
                              }
                              alt=""
                            />

                            <button
                              type="button"
                              onClick={() =>
                                removeImage(
                                  index
                                )
                              }
                            >
                              이미지 제거
                            </button>
                          </div>
                        ) : (
                          <label
                            className="donation-guide-image-upload"
                          >
                            <input
                              type="file"
                              accept="image/jpeg,image/png,image/webp"
                              onChange={
                                (event) =>
                                  handleImageChange(
                                    index,
                                    event
                                  )
                              }
                              disabled={
                                item.uploading
                              }
                            />

                            <span>
                              {item.uploading
                                ? '업로드 중...'
                                : '이미지 첨부'}
                            </span>

                            <small>
                              JPG · PNG · WEBP /
                              최대 5MB
                            </small>
                          </label>
                        )}
                      </div>
                    </div>


                    {form.items.length
                      > 1 && (
                      <button
                        type="button"
                        className="donation-guide-item-remove"
                        onClick={() =>
                          removeItem(
                            index
                          )
                        }
                      >
                        항목 삭제
                      </button>
                    )}
                  </article>
                )
              )}
            </div>


            {form.items.length
              < 12 && (
              <button
                type="button"
                className="donation-guide-item-add"
                onClick={
                  addItem
                }
              >
                + 안내 항목 추가
              </button>
            )}


            <div
              className="donation-guide-form-actions"
            >
              <button
                type="button"
                className="cancel"
                onClick={
                  closeForm
                }
              >
                취소
              </button>

              <button
                type="submit"
                className="save"
                disabled={
                  submitting
                }
              >
                {submitting
                  ? '저장 중...'
                  : editingId
                    ? '수정 완료'
                    : '게시하기'}
              </button>
            </div>
          </form>
        </section>
      )}


      <section
        className="donation-guide-list"
      >
        {loading ? (
          <div
            className="donation-guide-empty"
          >
            안내를 불러오는 중입니다.
          </div>
        ) : tips.length === 0 ? (
          <div
            className="donation-guide-empty"
          >
            등록된 안내가 없습니다.
          </div>
        ) : (
          tips.map(
            (tip) => (
              <article
                key={tip.id}
                className="donation-guide-post"
              >
                <header
                  className="donation-guide-post-header"
                >
                  <div>
                    <span
                      className="donation-guide-post-category"
                    >
                      {
                        tip.categoryLabel
                      }
                    </span>

                    <h2>
                      {tip.title}
                    </h2>

                    <p>
                      {formatDate(
                        tip.updateTime
                        || tip.regTime
                      )}
                    </p>
                  </div>


                  {isAdmin && (
                    <div
                      className="donation-guide-admin-actions"
                    >
                      <button
                        type="button"
                        onClick={() =>
                          openEditForm(
                            tip
                          )
                        }
                      >
                        수정
                      </button>

                      <button
                        type="button"
                        onClick={() =>
                          handleDelete(
                            tip.id
                          )
                        }
                      >
                        삭제
                      </button>
                    </div>
                  )}
                </header>


                <div
                  className="donation-guide-grid"
                >
                  {tip.items.map(
                    (item, index) => (
                      <article
                        key={
                          item.id
                          ?? index
                        }
                        className="donation-guide-item"
                      >
                        {item.imagePath ? (
                          <div
                            className="donation-guide-item-image"
                          >
                            <img
                              src={
                                item.imagePath
                              }
                              alt=""
                              loading="lazy"
                            />
                          </div>
                        ) : (
                          <div
                            className="donation-guide-item-image donation-guide-item-image--empty"
                            aria-hidden="true"
                          >
                            BloodOps
                          </div>
                        )}


                        <div
                          className="donation-guide-item-copy"
                        >
                          <span
                            className="donation-guide-item-number"
                          >
                            {String(
                              item.itemOrder
                              ?? index + 1
                            ).padStart(
                              2,
                              '0'
                            )}
                          </span>

                          <div>
                            {item.title && (
                              <strong>
                                {item.title}
                              </strong>
                            )}

                            <p>
                              {item.content}
                            </p>
                          </div>
                        </div>
                      </article>
                    )
                  )}
                </div>
              </article>
            )
          )
        )}
      </section>

    </main>
  )
}


export default DonationTipsPage