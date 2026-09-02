import {
  useEffect,
  useRef,
  useState,
} from 'react'

import {
  useNavigate,
} from 'react-router-dom'

import {
  sendChatbotMessage,
} from '../../api/apiClient.js'

import './ChatbotWidget.css'

const DEFAULT_MESSAGES = [
  {
    id: 1,
    role: 'assistant',
    text:
      '안녕하세요! BloodOps AI 안내 챗봇입니다. 헌혈 예약이나 사이트 이용방법을 물어보세요.',
    actions: [],
  },
]

const CHATBOT_STORAGE_KEY =
  'bloodops-chatbot-messages'

function ChatbotWidget() {
  const navigate = useNavigate()

  const [
    isOpen,
    setIsOpen,
  ] = useState(false)

  const [
    input,
    setInput,
  ] = useState('')

  const [
    messages,
    setMessages,
  ] = useState(() => {
    try {
      const savedMessages =
        sessionStorage.getItem(
          CHATBOT_STORAGE_KEY
        )

      if (savedMessages) {
        const parsedMessages =
          JSON.parse(savedMessages)

        if (
          Array.isArray(parsedMessages)
          && parsedMessages.length > 0
        ) {
          return parsedMessages
        }
      }
    } catch {
      // 저장 데이터가 손상된 경우
      // 기본 대화로 다시 시작합니다.
    }

    return DEFAULT_MESSAGES
  })

  const [
    isSending,
    setIsSending,
  ] = useState(false)

  const messagesEndRef = useRef(null)

  useEffect(() => {
    try {
      sessionStorage.setItem(
        CHATBOT_STORAGE_KEY,
        JSON.stringify(messages)
      )
    } catch {
      // sessionStorage를 사용할 수 없는 경우에는
      // 현재 화면에서만 대화를 유지합니다.
    }
  }, [messages])

  useEffect(() => {
    if (!isOpen) {
      return
    }

    messagesEndRef.current?.scrollIntoView({
      behavior: 'smooth',
      block: 'end',
    })
  }, [
    messages,
    isSending,
    isOpen,
  ])

  async function handleSubmit(event) {
    event.preventDefault()

    const message =
      input.trim()

    if (
      !message
      || isSending
    ) {
      return
    }

    const userMessage = {
      id: Date.now(),
      role: 'user',
      text: message,
      actions: [],
    }

    setMessages((current) => [
      ...current,
      userMessage,
    ])

    setInput('')
    setIsSending(true)

    try {
      const response =
        await sendChatbotMessage(
          message
        )

      setMessages((current) => [
        ...current,
        {
          id: Date.now() + 1,
          role: 'assistant',
          text:
            response.message
            || '답변을 불러오지 못했습니다.',
          intent:
            response.intent,
          actions:
            Array.isArray(response.actions)
              ? response.actions
              : [],
          sources:
            Array.isArray(response.sources)
              ? response.sources
              : [],
        },
      ])

    } catch (error) {
      setMessages((current) => [
        ...current,
        {
          id: Date.now() + 1,
          role: 'assistant',
          text:
            error.message
            || 'AI 안내 서비스 연결 중 오류가 발생했습니다.',
          actions: [],
          isError: true,
        },
      ])

    } finally {
      setIsSending(false)
    }
  }


  function handleAction(action) {
    if (
      action.type !== 'LINK'
      || !action.url
    ) {
      return
    }

    setIsOpen(false)
    navigate(action.url)
  }


  return (
    <div className="chatbot-widget">
      {isOpen && (
        <section
          className="chatbot-panel"
          aria-label="BloodOps AI 챗봇"
        >
          <header className="chatbot-header">
            <div>
              <strong>
                BloodOps AI
              </strong>

              <span>
                헌혈 · 사이트 이용 안내
              </span>
            </div>

            <button
              type="button"
              className="chatbot-close"
              onClick={() =>
                setIsOpen(false)
              }
              aria-label="챗봇 닫기"
            >
              ×
            </button>
          </header>

          <div className="chatbot-messages">
            {messages.map((message) => (
              <div
                key={message.id}
                className={
                  `chatbot-message-group ${
                    message.role
                  }`
                }
              >
                <div
                  className={
                    `chatbot-message ${
                      message.role
                    } ${
                      message.isError
                        ? 'error'
                        : ''
                    }`
                  }
                >
                  {message.text}
                </div>

                {message.actions?.length > 0 && (
                  <div className="chatbot-actions">
                    {message.actions.map(
                      (action, index) => (
                        <button
                          key={
                            `${action.label}-${index}`
                          }
                          type="button"
                          className="chatbot-action-button"
                          onClick={() =>
                            handleAction(action)
                          }
                        >
                          {action.label}
                        </button>
                      )
                    )}
                  </div>
                )}
              </div>
            ))}

            {isSending && (
              <div className="chatbot-message-group assistant">
                <div className="chatbot-message assistant loading">
                  답변을 작성하고 있습니다...
                </div>
              </div>
            )}
            <div
              ref={messagesEndRef}
              aria-hidden="true"
            />
          </div>

          <form
            className="chatbot-input-area"
            onSubmit={handleSubmit}
          >
            <input
              type="text"
              value={input}
              maxLength={1000}
              placeholder="궁금한 내용을 입력하세요."
              onChange={(event) =>
                setInput(
                  event.target.value
                )
              }
              disabled={isSending}
            />

            <button
              type="submit"
              disabled={
                isSending
                || !input.trim()
              }
            >
              전송
            </button>
          </form>
        </section>
      )}

      <button
        type="button"
        className="chatbot-toggle"
        onClick={() =>
          setIsOpen((current) =>
            !current
          )
        }
        aria-label="AI 챗봇 열기"
        aria-expanded={isOpen}
      >
        AI
      </button>
    </div>
  )
}


export default ChatbotWidget