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

const donationTipsDuration =
  new Trend(
    'duration_donation_tips',
    true
  )

const donationRankingDuration =
  new Trend(
    'duration_donation_ranking',
    true
  )


export const options = {
  stages: [
    {
      duration: '30s',
      target: 100,
    },
    {
      duration: '1m',
      target: 100,
    },

    {
      duration: '30s',
      target: 200,
    },
    {
      duration: '1m',
      target: 200,
    },

    {
      duration: '30s',
      target: 300,
    },
    {
      duration: '1m',
      target: 300,
    },

    {
      duration: '30s',
      target: 500,
    },
    {
      duration: '1m',
      target: 500,
    },

    {
      duration: '30s',
      target: 0,
    },
  ],

  thresholds: {
    http_req_failed: [
      'rate<0.05',
    ],

    http_req_duration: [
      'p(95)<2000',
    ],

    checks: [
      'rate>0.95',
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
    name: '헌혈 팁',
    path: '/api/donation-tips',
    metric: donationTipsDuration,
  },

  {
    name: '헌혈 랭킹',
    path: '/api/public/donation-rankings',
    metric: donationRankingDuration,
  },
]


export default function () {
  for (const endpoint of endpoints) {
    const response =
      http.get(
        `${BASE_URL}${endpoint.path}`
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

  sleep(0.2)
}