
const toggleSidebar = ()=>{

  if($(".sidebar").is(":visible"))
  {
  		$(".sidebar").css("display","none");
  		$(".content").css("margin-left","0%");
  }
  else{
  		$(".sidebar").css("display","block");
  		$(".content").css("margin-left","20%");
  }
}
const search=()=>{
  let q=$("#search-input").val();
 
  if(q=="")
  {
  	$(".search-result").hide();
  }
  else{

  let url=`http://localhost:1200/search/${q}`;

  fetch(url).
  then((response)=>{
  	return response.json(); 
  })
  .then((data)=>{
 	
 	let text=`<div class="list-group">`
 	
 	data.forEach((contact)=>{
 	text+=`<a href="/user/contact/${contact.cid}" class="list-group-item list-group-item-action">${contact.name}</a>`
 	});	
 	text+=`</div>`;
 	$(".search-result").html(text);
 	$(".search-result").show();
 });
   $(".search-result").show();
  }
};
const paymentStart = () => {

	console.log('paymentStart');
	const amt = $('#amount').val();
	console.log(amt);

	if (amt=="" || amt==null) {
		alert("Enter an valid amount");
		return;
	}

	$.ajax(
	{
		url:'/user/create_order',
		data:JSON.stringify({amount:amt,info:'order_req'}),
		contentType:'application/json',
		type:'POST',
		dataType:'json',
		success:function (response) {
			// body...
			console.log(response);
			if (response.statux == 'created') {
				//open payment form
				let option={
					key:'rzp_test_tdXePszMuggSb2',
					amount:response.amount,
					currency:'INR',
					name:'Smart Contact Manager Donation',
					image:'https://media.coindesk.com/uploads/2018/01/India-rupees.jpg',
					order_id=response.id,
				}
			}
		},
		error:function(error){
			console.log(error);
			alert('something went wrong')
		}
	}
	)

};