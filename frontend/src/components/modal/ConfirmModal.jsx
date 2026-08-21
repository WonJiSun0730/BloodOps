import './ConfirmModal.css'


function ConfirmModal({
  open,
  title,
  message,
  confirmText = '확인',
  cancelText = '취소',
  showCancel = true,
  danger = false,
  onConfirm,
  onCancel,
}) {
  if (!open) {
    return null
  }

  return (
    <div
      className="confirm-modal-backdrop"
      role="presentation"
      onMouseDown={onCancel}
    >
      <section
        className="confirm-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-modal-title"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <span className="confirm-modal-eyebrow">
          BLOODOPS
        </span>

        <h2 id="confirm-modal-title">
          {title}
        </h2>

        <p>
          {message}
        </p>

        <div className="confirm-modal-actions">
          {showCancel && (
            <button
              type="button"
              className="confirm-modal-cancel"
              onClick={onCancel}
            >
              {cancelText}
            </button>
          )}

          <button
            type="button"
            className={
              danger
                ? 'confirm-modal-confirm danger'
                : 'confirm-modal-confirm'
            }
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </section>
    </div>
  )
}


export default ConfirmModal