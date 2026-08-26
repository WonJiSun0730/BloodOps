import {
  useEffect,
  useRef,
  useState,
} from 'react'

let naverMapsLoadPromise = null

function loadNaverMaps() {
  if (window.naver?.maps) {
    return Promise.resolve(
      window.naver.maps
    )
  }

  if (naverMapsLoadPromise) {
    return naverMapsLoadPromise
  }

  const clientId =
    import.meta.env
      .VITE_NAVER_MAP_CLIENT_ID

  if (!clientId) {
    return Promise.reject(
      new Error(
        'NAVER Maps Client ID가 설정되지 않았습니다.'
      )
    )
  }

  naverMapsLoadPromise =
    new Promise((resolve, reject) => {
      const script =
        document.createElement('script')

      script.src =
        'https://oapi.map.naver.com/openapi/v3/maps.js'
        + `?ncpKeyId=${encodeURIComponent(clientId)}`

      script.async = true

      script.onload = () => {
        if (window.naver?.maps) {
          resolve(window.naver.maps)
          return
        }

        reject(
          new Error(
            'NAVER Maps API를 불러오지 못했습니다.'
          )
        )
      }

      script.onerror = () => {
        reject(
          new Error(
            'NAVER Maps 스크립트 요청에 실패했습니다.'
          )
        )
      }

      document.head.appendChild(
        script
      )
    })

  return naverMapsLoadPromise
}

function hasValidCoordinates(center) {
  const latitude =
    Number(center?.latitude)

  const longitude =
    Number(center?.longitude)

  // 대한민국 인근 범위를 벗어난 잘못된 좌표 제외
  return (
    Number.isFinite(latitude)
    && Number.isFinite(longitude)
    && latitude >= 32
    && latitude <= 40
    && longitude >= 123
    && longitude <= 133
  )
}

function createMarkerIcon(
  maps,
  selected,
  inactive
) {
  return {
    content: `
      <button
        type="button"
        class="center-map-marker${
          selected
            ? ' selected'
            : ''
        }${
          inactive
            ? ' inactive'
            : ''
        }"
        aria-label="${
          inactive
            ? '운영 중단 헌혈센터 위치'
            : '헌혈센터 위치'
        }"
        ${
          inactive
            ? 'disabled'
            : ''
        }
      >
        <span></span>
      </button>
    `,
    anchor:
      new maps.Point(
        11,
        11
      ),
  }
}

function CenterMapArea({
  centers,
  selectedCenter,
  onCenterSelect,
}) {
  const containerRef = useRef(null)
  const mapRef = useRef(null)

  const markerEntriesRef =
    useRef([])

  const selectHandlerRef =
    useRef(onCenterSelect)

  const [
    mapReady,
    setMapReady,
  ] = useState(false)

  const [
    mapError,
    setMapError,
  ] = useState('')

  useEffect(() => {
    selectHandlerRef.current =
      onCenterSelect
  }, [onCenterSelect])

  useEffect(() => {
    let active = true

    loadNaverMaps()
      .then((maps) => {
        if (
          !active
          || !containerRef.current
        ) {
          return
        }

        mapRef.current =
          new maps.Map(
            containerRef.current,
            {
              center:
                new maps.LatLng(
                  36.4,
                  127.8
                ),
              zoom: 7,
              minZoom: 6,
              zoomControl: true,
              zoomControlOptions: {
                position:
                  maps.Position.TOP_RIGHT,
              },
            }
          )

        setMapReady(true)
      })
      .catch((error) => {
        if (active) {
          setMapError(
            error.message
          )
        }
      })

    return () => {
      active = false

      markerEntriesRef.current
        .forEach(({ marker }) => {
          marker.setMap(null)
        })

      markerEntriesRef.current = []
      mapRef.current = null
    }
  }, [])

  /*
   * 검색 결과가 달라질 때만
   * 마커 전체를 다시 생성합니다.
   */
  useEffect(() => {
    if (
      !mapReady
      || !mapRef.current
      || !window.naver?.maps
    ) {
      return
    }

    const maps =
      window.naver.maps

    markerEntriesRef.current
      .forEach(({ marker }) => {
        marker.setMap(null)
      })

    markerEntriesRef.current = []

    const validCenters =
      centers.filter(
        hasValidCoordinates
      )

    if (validCenters.length === 0) {
      mapRef.current.setCenter(
        new maps.LatLng(
          36.4,
          127.8
        )
      )

      mapRef.current.setZoom(7)
      return
    }

    const firstPosition =
      new maps.LatLng(
        Number(
          validCenters[0].latitude
        ),
        Number(
          validCenters[0].longitude
        )
      )

    const bounds =
      new maps.LatLngBounds(
        firstPosition,
        firstPosition
      )

    validCenters.forEach(
      (center) => {
        const position =
          new maps.LatLng(
            Number(center.latitude),
            Number(center.longitude)
          )

        const inactive =
          center.active === false

        const marker =
          new maps.Marker({
            map: mapRef.current,
            position,
            title:
              center.centerName,
            icon:
              createMarkerIcon(
                maps,
                false,
                inactive
              ),
          })

        if (!inactive) {
          maps.Event.addListener(
            marker,
            'click',
            () => {
              selectHandlerRef.current?.(
                center.id
              )
            }
          )
        }

        markerEntriesRef.current.push({
          centerId: center.id,
          marker,
          position,
          inactive,
        })

        bounds.extend(position)
      }
    )

    if (validCenters.length === 1) {
      mapRef.current.setCenter(
        firstPosition
      )

      mapRef.current.setZoom(14)
      return
    }

    mapRef.current.fitBounds(
      bounds
    )
  }, [
    centers,
    mapReady,
  ])

  /*
   * 센터 선택 시 기존 마커 위치를 기준으로
   * 지도 중심과 줌을 즉시 변경합니다.
   */
  useEffect(() => {
    if (
      !mapReady
      || !mapRef.current
      || !window.naver?.maps
      || !selectedCenter
    ) {
      return
    }

    const maps =
      window.naver.maps

    const selectedEntry =
      markerEntriesRef.current
        .find(
          (entry) =>
            entry.centerId
            === selectedCenter.id
        )

    if (!selectedEntry) {
      return
    }

    markerEntriesRef.current
      .forEach((entry) => {
        const selected =
          entry.centerId
          === selectedCenter.id

        entry.marker.setZIndex(
          selected
            ? 200
            : 100
        )

        entry.marker.setIcon(
          createMarkerIcon(
            maps,
            selected,
            entry.inactive
          )
        )
      })

    /*
     * 전국 단위 panTo 애니메이션 도중
     * 줌을 변경하면 위치 계산이 꼬일 수 있으므로,
     * 줌과 중심을 애니메이션 없이 즉시 적용합니다.
     */
    mapRef.current.setZoom(
      14,
      false
    )

    mapRef.current.setCenter(
      selectedEntry.position
    )
  }, [
    centers,
    mapReady,
    selectedCenter,
  ])

  if (mapError) {
    return (
      <div className="center-map-error">
        {mapError}
      </div>
    )
  }

  return (
    <div className="center-map-wrapper">
      {!mapReady && (
        <div className="center-map-loading">
          지도를 불러오는 중입니다.
        </div>
      )}

      <div
        ref={containerRef}
        className="center-map"
        aria-label="헌혈센터 지도"
      />
    </div>
  )
}

export default CenterMapArea