function EyeIcon({
  visible = false,
  size = 20,
}) {
  if (visible) {
    return (
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        aria-hidden="true"
      >
        <path
          d="M3 3L21 21"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
        />

        <path
          d="M10.6 10.7A2 2 0 0 0 13.3 13.4"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
        />

        <path
          d="M9.4 5.4A10.2 10.2 0 0 1 12 5C17.2 5 21 12 21 12a16.4 16.4 0 0 1-2.1 3.2"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        <path
          d="M6.2 6.2C4.2 7.7 3 10 3 12c0 0 3.8 7 9 7 1.4 0 2.7-.5 3.8-1.2"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    )
  }

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M3 12s3.8-7 9-7 9 7 9 7-3.8 7-9 7-9-7-9-7Z"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />

      <circle
        cx="12"
        cy="12"
        r="2.4"
        stroke="currentColor"
        strokeWidth="1.8"
      />
    </svg>
  )
}

export default EyeIcon