import http from 'k6/http'
import {
  check,
  sleep,
} from 'k6'

import {
  Trend,
} from 'k6/metrics'


const BASE_URL =
  __ENV.BASE_URL
  || 'http://localhost:80'


const centerListDuration =
  new Trend(
    'duration_center_list',
    true
  )

const centerPageDuration =
  new Trend(
    'duration_center_page',
    true
  )

const centerMapDuration =
  new Trend(
    'duration_center_map',
    true
  )

const donationTipsDuration =
  new Trend(
    'duration_donation_tips',
    true
  )

const latestDonationTipDuration =
  new Trend(
    'duration_latest_donation_tip',
    true
  )

const donationRankingDuration =
  new Trend(
    'duration_donation_ranking',
    true
  )

const bloodStockDuration =
  new Trend(
    'duration_blood_stock',
    true
  )


export const options = {
  stages: [
    {
      duration: '20s',
      target: 10,
    },
    {
      duration: '40s',
      target: 10,
    },
    {
      duration: '20s',
      target: 30,
    },
    {
      duration: '40s',
      target: 30,
    },
    {
      duration: '20s',
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

  summaryTrendStats: [
    'avg',
    'med',
    'p(90)',
    'p(95)',
    'p(99)',
    'max',
  ],
}


const endpoints = [
  {
    name: '센터 목록',
    path: '/api/centers',
    metric: centerListDuration,
  },

  {
    name: '센터 페이지',
    path: '/api/centers/page?page=0',
    metric: centerPageDuration,
  },

  {
    name: '센터 지도',
    path: '/api/centers/map',
    metric: centerMapDuration,
  },

  {
    name: '헌혈 팁',
    path: '/api/donation-tips',
    metric: donationTipsDuration,
  },

  {
    name: '최신 헌혈 팁',
    path: '/api/donation-tips/latest',
    metric: latestDonationTipDuration,
  },

  {
    name: '헌혈 랭킹',
    path: '/api/public/donation-rankings',
    metric: donationRankingDuration,
  },

  {
    name: '혈액 보유 현황',
    path: '/api/public/blood-stock-status',
    metric: bloodStockDuration,
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

    endpoint.metric.add(
      response.timings.duration
    )

    check(
      response,
      {
        [`${endpoint.name} 200`]:
          (res) =>
            res.status === 200,
      }
    )

    sleep(0.1)
  }

  sleep(0.3)
}