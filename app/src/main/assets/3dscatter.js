{
// Load the graphs data by invoking the Java hook
var jsonValue = JSON.parse(Android.loadData());

if(jsonValue != null && jsonValue.x != null && jsonValue.x.length > 0) {
var trace2 = {
	x:jsonValue.x, y: jsonValue.y, z: jsonValue.z,
	mode: 'markers',
	marker: {
		color: 'rgb(127, 127, 127)',
		size: 5,
		symbol: 'circle',
		line: {
		color: 'rgb(204, 204, 204)',
		width: 1},
		opacity: 0.8},
	type: 'scatter3d'};
var data = [trace2];
var layout = {margin: {
	l: 0,
	r: 0,
	b: 0,
	t: 0
  }};

Plotly.newPlot('myDiv', data, layout);
}

};

function PlotlyUpdate (jsonValue1){

var jsonValue = JSON.parse(jsonValue1);

if(jsonValue != null && jsonValue.x != null && jsonValue.x.length > 0) {
var trace2 = {
	x:jsonValue.x, y: jsonValue.y, z: jsonValue.z,
	mode: 'markers',
	marker: {
		color: 'rgb(127, 127, 127)',
		size: 12,
		symbol: 'circle',
		line: {
		color: 'rgb(204, 204, 204)',
		width: 1},
		opacity: 0.8},
	type: 'scatter3d'};
var data = [trace2];
var layout = {margin: {
	l: 0,
	r: 0,
	b: 0,
	t: 0
  }};

Plotly.newPlot('myDiv', data, layout);
}
};
