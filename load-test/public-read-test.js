import http from 'k6/http'
import {
  check,
  sleep,
} from 'k6'


const BASE_URL =
  __ENV.BASE_URL
  || 'http://localhost:80'


export const options = {
  stages: [
    {
      duration: '30s',
      target: 10,
    },
    {
      duration: '1m',
      target: 10,
    },
    {
      duration: '30s',
      target: 30,
    },
    {
      duration: '1m',
      target: 30,
    },
    {
      duration: '30s',
      target: 50,
    },
    {
      duration: '1m',
      target: 50,
    },
    {
      duration: '20s',
      target: 0,
    },
  ],

  thresholds: {
    http_req_failed: [
      'rate<0.01',
    ],

    http_req_duration: [
      'p(95)<1000',
    ],

    checks: [
      'rate>0.99',
    ],
  },
}


const endpoints = [
  {
    name: '센터 목록',
    path: '/api/centers',
  },
  {
    name: '센터 페이지',
    path: '/api/centers/page?page=0',
  },
  {
    name: '센터 지도',
    path: '/api/centers/map',
  },
  {
    name: '헌혈 팁',
    path: '/api/donation-tips',
  },
  {
    name: '최신 헌혈 팁',
    path: '/api/donation-tips/latest',
  },
  {
    name: '헌혈 랭킹',
    path: '/api/public/donation-rankings',
  },
  {
    name: '혈액 보유 현황',
    path: '/api/public/blood-stock-status',
  },
]


export default function () {
  for (const endpoint of endpoints) {
    const response =
      http.get(
        `${BASE_URL}${endpoint.path}`,
        {
          tags: {
            endpoint:
              endpoint.name,
          },
        }
      )

    check(
      response,
      {
        [`${endpoint.name} 200`]:
          (res) =>
            res.status === 200,
      }
    )

    sleep(0.15)
  }

  sleep(0.5)
}