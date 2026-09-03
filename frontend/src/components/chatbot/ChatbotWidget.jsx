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
    sources: [],
  },
]

const CHATBOT_STORAGE_KEY =
  'bloodops-chatbot-messages'

const RECOMMENDED_QUESTIONS = [
  '현재 혈액 보유량 알려줘',
  '가장 빠르게 예약 가능한 센터 찾아줘',
  '헌혈확인증명서는 어떻게 발급해?',
  '지금 진행 중인 프로모션 알려줘',
]

function ChatbotWidget({
  isOpen,
  onOpenChange,
}) {
  const navigate = useNavigate()

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

  async function sendMessage(
    rawMessage,
    appendUserMessage = true
  ) {
    const message =
      rawMessage.trim()

    if (
      !message
      || isSending
    ) {
      return
    }

    if (appendUserMessage) {
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
    }

    setInput('')
    setIsSending(true)

    const history =
      messages
        .filter(
          (item) =>
            item.role === 'user'
            || item.role === 'assistant'
        )
        .filter(
          (item) =>
            !item.isError
        )
        .slice(-6)
        .map((item) => ({
          role: item.role,
          message: item.text,
        }))

    try {
      const response =
        await sendChatbotMessage(
          message,
          history
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
    } catch {
      setMessages((current) => [
        ...current,
        {
          id: Date.now() + 1,
          role: 'assistant',
          text:
            '답변을 불러오는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.',
          actions: [],
          isError: true,
          retryMessage: message,
        },
      ])
    } finally {
      setIsSending(false)
    }
  }


  function handleSubmit(event) {
    event.preventDefault()

    sendMessage(input)
  }


  function handleRecommendedQuestion(question) {
    sendMessage(question)
  }


  function handleRetry(message) {
    if (
      !message
      || isSending
    ) {
      return
    }

    /*
     * 직전 오류 말풍선을 제거한 뒤
     * 동일한 질문을 다시 요청합니다.
     */
    setMessages((current) =>
      current.filter(
        (item) =>
          !(
            item.isError
            && item.retryMessage === message
          )
      )
    )

    sendMessage(
      message,
      false
    )
  }


  function handleAction(action) {
    if (
      action.type !== 'LINK'
      || !action.url
    ) {
      return
    }

    onOpenChange(false)
    navigate(action.url)
  }

  function handleResetConversation() {
    if (isSending) {
      return
    }

    try {
      sessionStorage.removeItem(
        CHATBOT_STORAGE_KEY
      )
    } catch {
      // sessionStorage를 사용할 수 없어도
      // 현재 화면의 대화는 초기화합니다.
    }

    setMessages(
      DEFAULT_MESSAGES.map((message) => ({
        ...message,
      }))
    )

    setInput('')
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

            <div className="chatbot-header-actions">
              <button
                type="button"
                className="chatbot-reset"
                onClick={handleResetConversation}
                disabled={isSending}
                aria-label="대화 초기화"
                title="대화 초기화"
              >
                ↻
              </button>

              <button
                type="button"
                className="chatbot-close"
                onClick={() =>
                  onOpenChange(false)
                }
                aria-label="챗봇 닫기"
              >
                ×
              </button>
            </div>
          </header>

          <div className="chatbot-messages">
            {messages.length === 1 && (
              <div className="chatbot-recommendations">
                <span className="chatbot-recommendations-title">
                  이런 질문을 해보세요
                </span>

                <div className="chatbot-recommendation-list">
                  {RECOMMENDED_QUESTIONS.map(
                    (question) => (
                      <button
                        key={question}
                        type="button"
                        className="chatbot-recommendation-button"
                        onClick={() =>
                          handleRecommendedQuestion(
                            question
                          )
                        }
                        disabled={isSending}
                      >
                        {question}
                      </button>
                    )
                  )}
                </div>
              </div>
            )}
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

                {message.isError
                  && message.retryMessage
                  && (
                    <button
                      type="button"
                      className="chatbot-retry-button"
                      onClick={() =>
                        handleRetry(
                          message.retryMessage
                        )
                      }
                      disabled={isSending}
                    >
                      다시 시도
                    </button>
                  )}

                {message.role === 'assistant'
                  && message.sources?.length > 0
                  && (
                    <div className="chatbot-sources">
                      <span className="chatbot-sources-label">
                        출처
                      </span>

                      {message.sources.map(
                        (source, index) => (
                          <span
                            key={`${source}-${index}`}
                            className="chatbot-source"
                          >
                            {source}
                          </span>
                        )
                      )}
                    </div>
                  )}

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
          onOpenChange(!isOpen)
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