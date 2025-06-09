<!--<template>-->
<!--	<div class="error">-->
<!--		<div class="error-flex">-->
<!--			<div class="left">-->
<!--				<div class="left-item">-->
<!--					<div class="left-item-animation left-item-num">404</div>-->
<!--					<div class="left-item-animation left-item-title">{{ $t('message.notFound.foundTitle') }}</div>-->
<!--					<div class="left-item-animation left-item-msg">{{ $t('message.notFound.foundMsg') }}</div>-->
<!--					<div class="left-item-animation left-item-btn">-->
<!--						<el-button type="primary" round @click="onGoHome">{{ $t('message.notFound.foundBtn') }}</el-button>-->
<!--					</div>-->
<!--				</div>-->
<!--			</div>-->
<!--			<div class="right">-->
<!--				<img src="https://i.hd-r.cn/1a0d90a6c1e8b0184c7299dda713effd.png" />-->
<!--			</div>-->
<!--		</div>-->
<!--	</div>-->
<!--</template>-->

<!--<script>-->
<!--export default {-->
<!--	name: 'noFound',-->
<!--	methods: {-->
<!--		// 去首页-->
<!--		onGoHome() {-->
<!--			this.$router.push('/');-->
<!--		},-->
<!--	},-->
<!--};-->
<!--</script>-->

<!--<style scoped lang="scss">-->
<!--.error {-->
<!--	height: 100%;-->
<!--	background-color: white;-->
<!--	display: flex;-->
<!--	.error-flex {-->
<!--		margin: auto;-->
<!--		display: flex;-->
<!--		height: 350px;-->
<!--		width: 900px;-->
<!--		.left {-->
<!--			flex: 1;-->
<!--			height: 100%;-->
<!--			align-items: center;-->
<!--			display: flex;-->
<!--			.left-item {-->
<!--				.left-item-animation {-->
<!--					opacity: 0;-->
<!--					animation-name: error-num;-->
<!--					animation-duration: 0.5s;-->
<!--					animation-fill-mode: forwards;-->
<!--				}-->
<!--				.left-item-num {-->
<!--					color: #d6e0f6;-->
<!--					font-size: 55px;-->
<!--				}-->
<!--				.left-item-title {-->
<!--					font-size: 20px;-->
<!--					color: #333333;-->
<!--					margin: 15px 0 5px 0;-->
<!--					animation-delay: 0.1s;-->
<!--				}-->
<!--				.left-item-msg {-->
<!--					color: #c0bebe;-->
<!--					font-size: 12px;-->
<!--					margin-bottom: 30px;-->
<!--					animation-delay: 0.2s;-->
<!--				}-->
<!--				.left-item-btn {-->
<!--					animation-delay: 0.2s;-->
<!--				}-->
<!--			}-->
<!--		}-->
<!--		.right {-->
<!--			flex: 1;-->
<!--			opacity: 0;-->
<!--			animation-name: error-img;-->
<!--			animation-duration: 2s;-->
<!--			animation-fill-mode: forwards;-->
<!--			img {-->
<!--				width: 100%;-->
<!--				height: 100%;-->
<!--			}-->
<!--		}-->
<!--	}-->
<!--}-->
<!--</style>-->
<template>
	<html>
	<head>
		<meta charset="utf-8">
		<title>404 Rolling Box</title>
		<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Anton"/>
	</head>

	<body>
	<div class="rail">
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
		<div class="stamp four">4</div>
		<div class="stamp zero">0</div>
	</div>
	<div class="world">
		<div class="forward">
			<div class="box">
				<div class="wall"></div>
				<div class="wall"></div>
				<div class="wall"></div>
				<div class="wall"></div>
				<div class="wall"></div>
				<div class="wall"></div>
			</div>
		</div>
	</div>
	</body>
	</html>
</template>

<script></script>

<style scoped>
body {
	background: #000;
	height: 100vh;
	overflow: hidden;
	display: flex;
	font-family: "Anton", sans-serif;
	justify-content: center;
	align-items: center;
	perspective: 1000px;
}

div {
	transform-style: preserve-3d;
}

.rail {
	position: absolute;
	width: 100%;
	height: 100%;
	display: flex;
	justify-content: center;
	align-items: center;
	transform: rotateX(-30deg) rotateY(-30deg);
}
.rail .stamp {
	position: absolute;
	width: 200px;
	height: 200px;
	display: flex;
	justify-content: center;
	align-items: center;
	background: #141414;
	color: #fff;
	font-size: 7rem;
}
.rail .stamp:nth-child(1) {
	-webkit-animation: stampSlide 40000ms -2300ms linear infinite;
	animation: stampSlide 40000ms -2300ms linear infinite;
}
.rail .stamp:nth-child(2) {
	-webkit-animation: stampSlide 40000ms -4300ms linear infinite;
	animation: stampSlide 40000ms -4300ms linear infinite;
}
.rail .stamp:nth-child(3) {
	-webkit-animation: stampSlide 40000ms -6300ms linear infinite;
	animation: stampSlide 40000ms -6300ms linear infinite;
}
.rail .stamp:nth-child(4) {
	-webkit-animation: stampSlide 40000ms -8300ms linear infinite;
	animation: stampSlide 40000ms -8300ms linear infinite;
}
.rail .stamp:nth-child(5) {
	-webkit-animation: stampSlide 40000ms -10300ms linear infinite;
	animation: stampSlide 40000ms -10300ms linear infinite;
}
.rail .stamp:nth-child(6) {
	-webkit-animation: stampSlide 40000ms -12300ms linear infinite;
	animation: stampSlide 40000ms -12300ms linear infinite;
}
.rail .stamp:nth-child(7) {
	-webkit-animation: stampSlide 40000ms -14300ms linear infinite;
	animation: stampSlide 40000ms -14300ms linear infinite;
}
.rail .stamp:nth-child(8) {
	-webkit-animation: stampSlide 40000ms -16300ms linear infinite;
	animation: stampSlide 40000ms -16300ms linear infinite;
}
.rail .stamp:nth-child(9) {
	-webkit-animation: stampSlide 40000ms -18300ms linear infinite;
	animation: stampSlide 40000ms -18300ms linear infinite;
}
.rail .stamp:nth-child(10) {
	-webkit-animation: stampSlide 40000ms -20300ms linear infinite;
	animation: stampSlide 40000ms -20300ms linear infinite;
}
.rail .stamp:nth-child(11) {
	-webkit-animation: stampSlide 40000ms -22300ms linear infinite;
	animation: stampSlide 40000ms -22300ms linear infinite;
}
.rail .stamp:nth-child(12) {
	-webkit-animation: stampSlide 40000ms -24300ms linear infinite;
	animation: stampSlide 40000ms -24300ms linear infinite;
}
.rail .stamp:nth-child(13) {
	-webkit-animation: stampSlide 40000ms -26300ms linear infinite;
	animation: stampSlide 40000ms -26300ms linear infinite;
}
.rail .stamp:nth-child(14) {
	-webkit-animation: stampSlide 40000ms -28300ms linear infinite;
	animation: stampSlide 40000ms -28300ms linear infinite;
}
.rail .stamp:nth-child(15) {
	-webkit-animation: stampSlide 40000ms -30300ms linear infinite;
	animation: stampSlide 40000ms -30300ms linear infinite;
}
.rail .stamp:nth-child(16) {
	-webkit-animation: stampSlide 40000ms -32300ms linear infinite;
	animation: stampSlide 40000ms -32300ms linear infinite;
}
.rail .stamp:nth-child(17) {
	-webkit-animation: stampSlide 40000ms -34300ms linear infinite;
	animation: stampSlide 40000ms -34300ms linear infinite;
}
.rail .stamp:nth-child(18) {
	-webkit-animation: stampSlide 40000ms -36300ms linear infinite;
	animation: stampSlide 40000ms -36300ms linear infinite;
}
.rail .stamp:nth-child(19) {
	-webkit-animation: stampSlide 40000ms -38300ms linear infinite;
	animation: stampSlide 40000ms -38300ms linear infinite;
}
.rail .stamp:nth-child(20) {
	-webkit-animation: stampSlide 40000ms -40300ms linear infinite;
	animation: stampSlide 40000ms -40300ms linear infinite;
}

@-webkit-keyframes stampSlide {
	0% {
		transform: rotateX(90deg) rotateZ(-90deg) translateZ(-200px) translateY(130px);
	}
	100% {
		transform: rotateX(90deg) rotateZ(-90deg) translateZ(-200px) translateY(-3870px);
	}
}

@keyframes stampSlide {
	0% {
		transform: rotateX(90deg) rotateZ(-90deg) translateZ(-200px) translateY(130px);
	}
	100% {
		transform: rotateX(90deg) rotateZ(-90deg) translateZ(-200px) translateY(-3870px);
	}
}
.world {
	transform: rotateX(-30deg) rotateY(-30deg);
}
.world .forward {
	position: absolute;
	-webkit-animation: slide 2000ms linear infinite;
	animation: slide 2000ms linear infinite;
}
.world .box {
	width: 200px;
	height: 200px;
	transform-origin: 100% 100%;
	-webkit-animation: roll 2000ms cubic-bezier(1, 0.01, 1, 1) infinite;
	animation: roll 2000ms cubic-bezier(1, 0.01, 1, 1) infinite;
}
.world .box .wall {
	position: absolute;
	width: 200px;
	height: 200px;
	background: rgba(10, 10, 10, 0.8);
	border: 1px solid #fafafa;
	box-sizing: border-box;
}
.world .box .wall::before {
	content: "";
	position: absolute;
	width: 100%;
	height: 100%;
	display: flex;
	justify-content: center;
	align-items: center;
	color: #fff;
	font-size: 7rem;
}
.world .box .wall:nth-child(1) {
	transform: translateZ(100px);
}
.world .box .wall:nth-child(2) {
	transform: rotateX(180deg) translateZ(100px);
}
.world .box .wall:nth-child(3) {
	transform: rotateX(90deg) translateZ(100px);
}
.world .box .wall:nth-child(3)::before {
	transform: rotateX(180deg) rotateZ(90deg) translateZ(-1px);
	-webkit-animation: zeroFour 4000ms -2000ms linear infinite;
	animation: zeroFour 4000ms -2000ms linear infinite;
}
.world .box .wall:nth-child(4) {
	transform: rotateX(-90deg) translateZ(100px);
}
.world .box .wall:nth-child(4)::before {
	transform: rotateX(180deg) rotateZ(-90deg) translateZ(-1px);
	-webkit-animation: zeroFour 4000ms -2000ms linear infinite;
	animation: zeroFour 4000ms -2000ms linear infinite;
}
.world .box .wall:nth-child(5) {
	transform: rotateY(90deg) translateZ(100px);
}
.world .box .wall:nth-child(5)::before {
	transform: rotateX(180deg) translateZ(-1px);
	-webkit-animation: zeroFour 4000ms linear infinite;
	animation: zeroFour 4000ms linear infinite;
}
.world .box .wall:nth-child(6) {
	transform: rotateY(-90deg) translateZ(100px);
}
.world .box .wall:nth-child(6)::before {
	transform: rotateX(180deg) rotateZ(180deg) translateZ(-1px);
	-webkit-animation: zeroFour 4000ms linear infinite;
	animation: zeroFour 4000ms linear infinite;
}

@-webkit-keyframes zeroFour {
	0% {
		content: "4";
	}
	100% {
		content: "0";
	}
}

@keyframes zeroFour {
	0% {
		content: "4";
	}
	100% {
		content: "0";
	}
}
@-webkit-keyframes roll {
	0% {
		transform: rotateZ(0deg);
	}
	85% {
		transform: rotateZ(90deg);
	}
	87% {
		transform: rotateZ(88deg);
	}
	90% {
		transform: rotateZ(90deg);
	}
	100% {
		transform: rotateZ(90deg);
	}
}
@keyframes roll {
	0% {
		transform: rotateZ(0deg);
	}
	85% {
		transform: rotateZ(90deg);
	}
	87% {
		transform: rotateZ(88deg);
	}
	90% {
		transform: rotateZ(90deg);
	}
	100% {
		transform: rotateZ(90deg);
	}
}
@-webkit-keyframes slide {
	0% {
		transform: translateX(0);
	}
	100% {
		transform: translateX(-200px);
	}
}
@keyframes slide {
	0% {
		transform: translateX(0);
	}
	100% {
		transform: translateX(-200px);
	}
}
</style>
