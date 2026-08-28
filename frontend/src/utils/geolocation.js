/*
 * 브라우저 현재 위치 조회
 *
 * 호출 시 브라우저의 위치 권한 요청이 발생할 수 있습니다.
 * 권한을 거부해도 기존 서비스 이용에는 영향을 주지 않습니다.
 */
export function getCurrentPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(
        new Error(
          '현재 브라우저에서는 위치 기능을 사용할 수 없습니다.'
        )
      )
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude:
            position.coords.latitude,
          longitude:
            position.coords.longitude,
        })
      },
      (error) => {
        if (error.code === 1) {
          reject(
            new Error(
              '위치 권한이 허용되지 않았습니다. 브라우저 설정에서 위치 권한을 확인해주세요.'
            )
          )
          return
        }

        if (error.code === 2) {
          reject(
            new Error(
              '현재 위치를 확인할 수 없습니다. 잠시 후 다시 시도해주세요.'
            )
          )
          return
        }

        if (error.code === 3) {
          reject(
            new Error(
              '위치 확인 시간이 초과되었습니다. 다시 시도해주세요.'
            )
          )
          return
        }

        reject(
          new Error(
            '현재 위치를 불러오지 못했습니다.'
          )
        )
      },
      {
        enableHighAccuracy: false,
        timeout: 10000,
        maximumAge: 300000,
      }
    )
  })
}


/*
 * 두 좌표 사이의 직선거리를 km 단위로 계산합니다.
 * Haversine 공식을 사용합니다.
 */
export function calculateDistanceKm(
  latitude1,
  longitude1,
  latitude2,
  longitude2
) {
  const earthRadiusKm = 6371

  const toRadians = (degree) =>
    degree * (Math.PI / 180)

  const latitudeDistance =
    toRadians(
      latitude2 - latitude1
    )

  const longitudeDistance =
    toRadians(
      longitude2 - longitude1
    )

  const firstLatitude =
    toRadians(latitude1)

  const secondLatitude =
    toRadians(latitude2)

  const haversine =
    Math.sin(
      latitudeDistance / 2
    ) ** 2
    + Math.cos(firstLatitude)
    * Math.cos(secondLatitude)
    * Math.sin(
      longitudeDistance / 2
    ) ** 2

  const centralAngle =
    2 * Math.atan2(
      Math.sqrt(haversine),
      Math.sqrt(1 - haversine)
    )

  return earthRadiusKm
    * centralAngle
}


/*
 * 센터 목록에서 사용자 위치와
 * 가장 가까운 운영 센터를 찾습니다.
 */
export function findNearestCenter(
  centers,
  userLatitude,
  userLongitude
) {
  const availableCenters =
    centers
      .filter(
        (center) =>
          center.active !== false
          && Number.isFinite(
            Number(center.latitude)
          )
          && Number.isFinite(
            Number(center.longitude)
          )
      )
      .map(
        (center) => ({
          ...center,
          distanceKm:
            calculateDistanceKm(
              userLatitude,
              userLongitude,
              Number(center.latitude),
              Number(center.longitude)
            ),
        })
      )
      .sort(
        (first, second) =>
          first.distanceKm
          - second.distanceKm
      )

  return availableCenters[0]
    || null
}