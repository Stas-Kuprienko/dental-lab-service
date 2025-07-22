function openDialog(rowId) {
	    var content = 'Load data for row ' + rowId + ' and populate the form fields';
	    var itemKey = $('#map .tr[data-rowid="' + rowId + '"] .td:first-child').text();

	    $('#dialog').dialog({
	        title: itemKey + '- - -',
	        modal: true,
	        buttons: {
	            Save: function() {
	                var editedData = {
	                    price: $('#value').val(),
	                };

	                $.ajax({
	                    url: 'http://localhost:8081/dental/main/product-map/' + rowId,
	                    type: 'POST',
	                    data: 'price=' + editedData.price + '&title=' + itemKey + '&method=put',
	                    contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
	                    success: function(response) {
	                        console.log('POST request successful');
	                        console.log(response);
	                        location.reload();
	                    },
	                    error: function(xhr, status, error) {
	                        console.log('POST request failed');
	                        console.log(xhr.responseText);
	                    }
	                });

	            },

                Delete: function() {
                  if (confirm('delete the product item?')) {
                    $.ajax({
                      url: 'http://localhost:8081/dental/main/product-map/' + rowId,
                      type: 'POST',
                      data: 'method=delete&title=' + itemKey,
                      contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
                      success: function(response) {
                        console.log('POST request successful');
                        console.log(response);
	                    location.reload();
                      },
                      error: function(xhr, status, error) {
                        console.log('POST request failed');
                        console.log(xhr.responseText);
                      }
                    });

	                }
	            }
	        },
	    });
	}