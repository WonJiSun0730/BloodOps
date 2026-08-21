function CenterMapArea({
  center,
}) {
  return (
    <div className="center-map-placeholder">
      {center
        ? (
          <>
            <strong>
              {center.centerName}
            </strong>

            <span>
              지도 API 연동 예정
            </span>
          </>
        )
        : '지도 API 연동 영역'}
    </div>
  )
}

export default CenterMapArea