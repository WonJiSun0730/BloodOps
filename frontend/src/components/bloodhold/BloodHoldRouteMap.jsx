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
        document.createElement(
          'script'
        )

      script.src =
        'https://oapi.map.naver.com/openapi/v3/maps.js'
        + `?ncpKeyId=${encodeURIComponent(clientId)}`

      script.async = true

      script.onload = () => {
        if (window.naver?.maps) {
          resolve(
            window.naver.maps
          )

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


function BloodHoldRouteMap({
  userPosition,
  target,
  routePath = [],
}) {
  const containerRef =
    useRef(null)

  const mapRef =
    useRef(null)

  const markersRef =
    useRef([])

  const polylineRef =
    useRef(null)

  const [
    mapError,
    setMapError,
  ] = useState('')


  useEffect(() => {
    let active = true

    async function renderMap() {
      try {
        setMapError('')

        const maps =
          await loadNaverMaps()

        if (
          !active
          || !containerRef.current
        ) {
          return
        }

        const userLatitude =
          Number(
            userPosition?.latitude
          )

        const userLongitude =
          Number(
            userPosition?.longitude
          )

        const targetLatitude =
          Number(
            target?.latitude
          )

        const targetLongitude =
          Number(
            target?.longitude
          )

        if (
          !Number.isFinite(
            userLatitude
          )
          || !Number.isFinite(
            userLongitude
          )
          || !Number.isFinite(
            targetLatitude
          )
          || !Number.isFinite(
            targetLongitude
          )
        ) {
          throw new Error(
            '지도에 표시할 위치 정보가 올바르지 않습니다.'
          )
        }

        markersRef.current
          .forEach((marker) => {
            marker.setMap(null)
          })

        markersRef.current = []

        polylineRef.current
          ?.setMap(null)

        const userLatLng =
          new maps.LatLng(
            userLatitude,
            userLongitude
          )

        const targetLatLng =
          new maps.LatLng(
            targetLatitude,
            targetLongitude
          )

        if (!mapRef.current) {
          mapRef.current =
            new maps.Map(
              containerRef.current,
              {
                center:
                  targetLatLng,

                zoom: 13,

                zoomControl: true,

                zoomControlOptions: {
                  position:
                    maps.Position.TOP_RIGHT,
                },
              }
            )
        }

        const userMarker =
          new maps.Marker({
            map:
              mapRef.current,

            position:
              userLatLng,

            title:
              '현재 위치',

            icon: {
              content: `
                <div class="blood-hold-map-marker user">
                  <span></span>
                  <strong>내 위치</strong>
                </div>
              `,
              anchor:
                new maps.Point(
                  18,
                  18
                ),
            },
          })

        const targetMarker =
          new maps.Marker({
            map:
              mapRef.current,

            position:
              targetLatLng,

            title:
              target.name
              || '목적지',

            icon: {
              content: `
                <div class="blood-hold-map-marker target">
                  <span></span>
                  <strong>${
                    target.name
                    || '목적지'
                  }</strong>
                </div>
              `,
              anchor:
                new maps.Point(
                  18,
                  18
                ),
            },
          })

        markersRef.current = [
          userMarker,
          targetMarker,
        ]

        const routeLatLngs =
          routePath
            .map((point) => {
              const latitude =
                Number(
                  point?.latitude
                )

              const longitude =
                Number(
                  point?.longitude
                )

              if (
                !Number.isFinite(latitude)
                || !Number.isFinite(longitude)
              ) {
                return null
              }

              return new maps.LatLng(
                latitude,
                longitude
              )
            })
            .filter(Boolean)


        if (routeLatLngs.length < 2) {
          throw new Error(
            '지도에 표시할 자동차 경로 정보가 없습니다.'
          )
        }


        polylineRef.current =
          new maps.Polyline({
            map:
              mapRef.current,

            path:
              routeLatLngs,

            strokeWeight: 5,

            strokeOpacity: 0.85,

            strokeLineCap: 'round',

            strokeLineJoin: 'round',
          })

        const bounds =
          new maps.LatLngBounds(
            routeLatLngs[0],
            routeLatLngs[0]
          )


        routeLatLngs.forEach(
          (latLng) => {
            bounds.extend(
              latLng
            )
          }
        )

        mapRef.current.fitBounds(
          bounds
        )

      } catch (error) {
        if (active) {
          setMapError(
            error.message
            || '지도를 불러오지 못했습니다.'
          )
        }
      }
    }

    renderMap()

    return () => {
      active = false
    }
  }, [
    routePath,
    target,
    userPosition,
  ])


  useEffect(() => {
    return () => {
      markersRef.current
        .forEach((marker) => {
          marker.setMap(null)
        })

      markersRef.current = []

      polylineRef.current
        ?.setMap(null)

      polylineRef.current = null
      mapRef.current = null
    }
  }, [])


  if (mapError) {
    return (
      <div className="blood-hold-map-error">
        {mapError}
      </div>
    )
  }


  return (
    <div
      ref={containerRef}
      className="blood-hold-route-map"
      aria-label={
        `현재 위치에서 ${target?.name || '목적지'}까지 자동차 경로 지도`
      }np
    />
  )
}


export default BloodHoldRouteMap