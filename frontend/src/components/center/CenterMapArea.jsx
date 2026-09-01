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

function resetToDefaultViewport(
  maps,
  map
) {
  map.setZoom(
    7,
    false
  )

  map.setCenter(
    new maps.LatLng(
      36.4,
      127.8
    )
  )
}

function getMarkerMode(zoom) {
  if (zoom >= 9) {
    return 'pin'
  }

  if (zoom >= 7) {
    return 'region'
  }

  return 'national'
}

function createMarkerIcon(
  maps,
  selected,
  inactive,
  zoom
) {
  const markerMode =
    getMarkerMode(zoom)

  const pinMode =
    markerMode === 'pin'

  const width =
    pinMode
      ? 30
      : markerMode === 'region'
        ? 22
        : 16

  const height =
    pinMode
      ? 38
      : width

  return {
    content: `
      <div
        class="center-map-marker marker-${markerMode}${
          selected
            ? ' selected'
            : ''
        }${
          inactive
            ? ' inactive'
            : ''
        }"
        role="img"
        aria-label="${
          inactive
            ? '운영 중단 헌혈센터 위치'
            : '헌혈센터 위치'
        }"
        aria-disabled="${
          inactive
            ? 'true'
            : 'false'
        }"
      >
        <span
          class="center-map-marker-shape"
          aria-hidden="true"
        >
          <i></i>
        </span>
      </div>
    `,
    size:
      new maps.Size(
        width,
        height
      ),
    anchor:
      new maps.Point(
        width / 2,
        height
      ),
  }
}

function createMarkerInfoContent(
  center
) {
  const content =
    document.createElement('div')

  content.className =
    center.active === false
      ? 'center-map-info inactive'
      : 'center-map-info'

  const name =
    document.createElement('strong')

  name.textContent =
    center.centerName
    || '헌혈센터'

  const status =
    document.createElement('span')

  status.textContent =
    center.active === false
      ? '운영 중단'
      : '운영 중 · 예약 가능'

  content.append(
    name,
    status
  )

  return content
}

function CenterMapArea({
  centers,
  selectedCenter,
  onCenterSelect,
  defaultViewport = false,
}) {
  const containerRef = useRef(null)
  const mapRef = useRef(null)

  const markerEntriesRef =
    useRef([])

  const infoWindowRef =
    useRef(null)

  const zoomListenerRef =
    useRef(null)

  const selectedCenterRef =
    useRef(selectedCenter)

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
    selectedCenterRef.current =
      selectedCenter
  }, [selectedCenter])

function updateMarkerIcons() {
  if (
    !mapRef.current
    || !window.naver?.maps
  ) {
    return
  }

  const maps =
    window.naver.maps

  const zoom =
    mapRef.current.getZoom()

  markerEntriesRef.current
    .forEach((entry) => {
      const selected =
        entry.centerId
        === selectedCenterRef.current?.id

      entry.marker.setIcon(
        createMarkerIcon(
          maps,
          selected,
          entry.inactive,
          zoom
        )
      )
    })
}

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

        zoomListenerRef.current =
          maps.Event.addListener(
            mapRef.current,
            'zoom_changed',
            updateMarkerIcons
          )

        infoWindowRef.current =
          new maps.InfoWindow({
            borderWidth: 0,
            backgroundColor:
              'transparent',
            disableAnchor: true,
            disableAutoPan: true,
            pixelOffset:
              new maps.Point(
                0,
                -14
              ),
          })

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

      if (
        zoomListenerRef.current
        && window.naver?.maps
      ) {
        window.naver.maps.Event
          .removeListener(
            zoomListenerRef.current
          )
      }

      zoomListenerRef.current = null

      infoWindowRef.current?.close()
      infoWindowRef.current = null

      markerEntriesRef.current
        .forEach(({ marker }) => {
          window.naver?.maps?.Event
            .clearInstanceListeners(
              marker
            )

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

    infoWindowRef.current?.close()

    markerEntriesRef.current
      .forEach(({ marker }) => {
        maps.Event.clearInstanceListeners(
          marker
        )

        marker.setMap(null)
      })

    markerEntriesRef.current = []

    const validCenters =
      centers.filter(
        hasValidCoordinates
      )

    if (validCenters.length === 0) {
      resetToDefaultViewport(
        maps,
        mapRef.current
      )

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
            title: '',
            clickable: true,
            cursor:
              inactive
                ? 'default'
                : 'pointer',
            icon:
              createMarkerIcon(
                maps,
                false,
                inactive,
                mapRef.current.getZoom()
              ),
          })

        maps.Event.addListener(
          marker,
          'mouseover',
          () => {
            if (!infoWindowRef.current) {
              return
            }

            infoWindowRef.current
              .setContent(
                createMarkerInfoContent(
                  center
                )
              )

            infoWindowRef.current.open(
              mapRef.current,
              marker
            )
          }
        )

        maps.Event.addListener(
          marker,
          'mouseout',
          () => {
            infoWindowRef.current?.close()
          }
        )

        if (!inactive) {
          maps.Event.addListener(
            marker,
            'click',
            () => {
              /*
               * 모바일에서는 hover가 없으므로
               * 터치 시 센터명 UI도 함께 표시합니다.
               */
              if (infoWindowRef.current) {
                infoWindowRef.current
                  .setContent(
                    createMarkerInfoContent(
                      center
                    )
                  )

                infoWindowRef.current.open(
                  mapRef.current,
                  marker
                )
              }

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

    if (defaultViewport) {
      resetToDefaultViewport(
        maps,
        mapRef.current
      )

      return
    }

    if (validCenters.length === 1) {
      mapRef.current.setZoom(
        12,
        false
      )

      mapRef.current.setCenter(
        firstPosition
      )

      return
    }

    mapRef.current.fitBounds(
      bounds
    )
  }, [
    centers,
    defaultViewport,
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
            entry.inactive,
            mapRef.current.getZoom()
          )
        )
      })

    /*
     * 중심 좌표와 줌을 함께 변경해
     * 센터 선택 시 지도 이동을 부드럽게 처리합니다.
     */
    mapRef.current.morph(
      selectedEntry.position,
      12
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