import { useEffect, useRef } from 'react'
import * as echarts from 'echarts/core'
import { SVGRenderer } from 'echarts/renderers'
import 'echarts-liquidfill'

echarts.use([SVGRenderer])

const DROP_SHAPE =
  'path://M50 3C42 20 17 45 17 72a33 33 0 0 0 66 0C83 45 58 20 50 3Z'

function LiquidBloodDrop({ bloodType, value }) {
  const chartElementRef = useRef(null)

  useEffect(() => {
    const chartElement = chartElementRef.current

    if (!chartElement) {
      return undefined
    }

    const chart = echarts.init(chartElement, null, {
      renderer: 'svg',
    })
    const reducedMotion = window.matchMedia(
      '(prefers-reduced-motion: reduce)'
    ).matches
    const fillRatio = Math.min(
      1,
      Math.max(0, Number(value) / 10)
    )

    chart.setOption({
      animation: !reducedMotion,
      series: [
        {
          type: 'liquidFill',
          shape: DROP_SHAPE,
          radius: '92%',
          center: ['50%', '50%'],
          data: [
            {
              value: fillRatio,
              direction: 'right',
              itemStyle: { color: '#d71920' },
            },
            {
              value: Math.max(0, fillRatio - 0.025),
              direction: 'left',
              itemStyle: { color: '#ef5350', opacity: 0.72 },
            },
          ],
          amplitude: reducedMotion ? 0 : '7%',
          waveLength: '75%',
          period: reducedMotion ? 0 : 2600,
          waveAnimation: !reducedMotion,
          animationDuration: reducedMotion ? 0 : 900,
          animationDurationUpdate: reducedMotion ? 0 : 700,
          backgroundStyle: {
            color: 'rgba(115, 121, 128, 0.08)',
          },
          outline: {
            show: true,
            borderDistance: 2,
            itemStyle: {
              borderColor: 'rgba(115, 121, 128, 0.42)',
              borderWidth: 2,
              shadowBlur: 0,
            },
          },
          label: {
            show: true,
            formatter: bloodType,
            color: '#2b2f33',
            insideColor: '#f5f5f5',
            fontSize: 18,
            fontWeight: 900,
          },
          emphasis: { disabled: true },
          silent: true,
        },
      ],
    })

    const resizeObserver = new ResizeObserver(() => {
      chart.resize()
    })
    resizeObserver.observe(chartElement)

    return () => {
      resizeObserver.disconnect()
      chart.dispose()
    }
  }, [bloodType, value])

  return (
    <div
      ref={chartElementRef}
      className="blood-drop"
      aria-hidden="true"
    />
  )
}

export default LiquidBloodDrop
