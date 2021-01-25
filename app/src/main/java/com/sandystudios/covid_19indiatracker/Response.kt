package com.sandystudios.covid_19indiatracker

data class Response(
	val statewise: List<StatewiseItem>
)

data class StatewiseItem(
	val deltarecovered: String,
	val deltaactive: String,
	val deltaconfirmed: String,
	val deltadeaths: String,

	val recovered: String,
	val active: String,
	val state: String,
	val confirmed: String,
	val deaths: String,
	val lastupdatedtime: String
)

