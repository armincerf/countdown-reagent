export function drawClock(ctx, secs) {
  /* parameters */
  //var dim = canvas.width();
  var dim = 250
  var mid = dim / 2

  ctx.clearRect(0, 0, dim, dim)

  /* outer rings */
  ctx.beginPath()
  ctx.arc(mid, mid, mid - 5, 0, 2 * Math.PI)
  ctx.strokeStyle = 'rgb(127, 127, 127)' // grey
  ctx.lineWidth = 5
  ctx.stroke()

  ctx.beginPath()
  ctx.arc(mid, mid, mid - 7, 0, 2 * Math.PI)
  ctx.strokeStyle = 'rgb(34, 81, 103)' // dark blue
  ctx.fillStyle = 'rgb(255, 255, 218)' // light yellow
  ctx.lineWidth = 7
  ctx.fill()
  ctx.stroke()

  /* lit-up area */
  var insideClock = 11

  ctx.strokeStyle = 'rgb(251, 245, 88)' // bright yellow
  ctx.lineWidth = 7
  for (var a = 0; a <= 30 - secs; a++) {
    if (a % 15 == 0) continue
    ctx.beginPath()
    ctx.moveTo(
      mid + (mid - insideClock - 2) * Math.sin((Math.PI * 2 * a) / 60),
      mid - (mid - insideClock - 2) * Math.cos((Math.PI * 2 * a) / 60)
    )
    ctx.lineTo(
      mid + (mid - insideClock - 40) * Math.sin((Math.PI * 2 * a) / 60),
      mid - (mid - insideClock - 40) * Math.cos((Math.PI * 2 * a) / 60)
    )
    ctx.stroke()
  }

  /* pips */
  ctx.strokeStyle = 'rgb(59, 56, 56)' // grey
  ctx.lineWidth = 2
  ctx.fillStyle = 'rgb(255, 255, 255, 0.7)' // white
  for (var a = 0; a < 60; a += 5) {
    // grey line
    ctx.beginPath()
    ctx.moveTo(
      mid + (mid - insideClock - 1) * Math.sin((Math.PI * 2 * a) / 60),
      mid + (mid - insideClock - 1) * Math.cos((Math.PI * 2 * a) / 60)
    )
    ctx.lineTo(
      mid + (mid - insideClock - 40) * Math.sin((Math.PI * 2 * a) / 60),
      mid + (mid - insideClock - 40) * Math.cos((Math.PI * 2 * a) / 60)
    )
    ctx.stroke()
    // white dot
    ctx.beginPath()
    ctx.arc(
      mid + (mid - 7) * Math.sin((Math.PI * 2 * a) / 60),
      mid + (mid - 7) * Math.cos((Math.PI * 2 * a) / 60),
      2,
      0,
      2 * Math.PI
    )
    ctx.fill()
  }

  /* weird cross thing */
  ctx.beginPath()
  ctx.lineWidth = 3
  ctx.moveTo(mid, insideClock)
  ctx.lineTo(mid, dim - insideClock)
  ctx.moveTo(insideClock, mid)
  ctx.lineTo(dim - insideClock, mid)
  ctx.stroke()

  /* hand */
  ctx.fillStyle = 'rgb(31, 71, 132)' // blue
  ctx.strokeStyle = 'rgb(127, 121, 109)' // grey
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.arc(mid, mid, 8, (Math.PI * 2 * (-secs + 10)) / 60, (Math.PI * 2 * (-secs + 20)) / 60, true)
  ctx.lineTo(
    mid + (mid - insideClock - 5) * Math.sin((Math.PI * 2 * secs) / 60),
    mid + (mid - insideClock - 5) * Math.cos((Math.PI * 2 * secs) / 60)
  )
  ctx.fill()
  ctx.stroke()
}
