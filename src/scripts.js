
function removeNonPrintableTitles() {
	var spans = document.getElementsByTagName('span');
	var span;
	var i;
	for (i in spans) {
		span = spans[i];
		if (span.className == 'price') {
			span.removeAttribute('title');
		}
	}
}

window.onload = removeNonPrintableTitles;
