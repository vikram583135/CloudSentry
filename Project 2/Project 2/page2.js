document.addEventListener("DOMContentLoaded", ()=>{

    /*page 2 features */
    const firstdd = document.getElementById("FIRST-DD");
    const seconddd = document.getElementById("SECOND-DD");
    const thirddd = document.getElementById("THIRD-DD");
    const first = document.getElementById("FIRST");
    const second = document.getElementById("SECOND");
    const third = document.getElementById("THIRD");
    const back = document.getElementById("BACK");
    const request = document.getElementById("REQUEST");
    const attendance  = document.getElementById("ATTENDANCE");
    const pudisplay1 = document.getElementById("P-U-DISPLAY1");
    const pudisplay2 = document.getElementById("P-U-DISPLAY2");
    const xbtn1 = document.getElementById("X-BTN1");
    const xbtn2 = document.getElementById("X-BTN2");
    const ia = document.getElementById("IA");


    firstdd.style.display = "none";
    seconddd.style.display = "none";
    thirddd.style.display = "none";
    first.style.display = "none";
    second.style.display = "none";
    third.style.display = "none";

    let x = localStorage.getItem("semester");
    switch(x){
        case "f" : firstdd.style.display = "block";
                    first.style.display = "block";
                     break;
        case "s" : seconddd.style.display = "block";
                    second.style.display = "block";
                     break;
        case "t" : thirddd.style.display = "block";
                    third.style.display = "block";
                     break;
        default  : alert("Please go back and start anew");
    }

    back.addEventListener("click", ()=>{
        window.location.href = "index.html";
    });

    request.addEventListener("click", ()=>{
        window.open("./page3.html", "_blank");
    });

    pudisplay1.style.display = "none";
    pudisplay2.style.display = "none";
    attendance.addEventListener("click", ()=>{
        pudisplay1.style.display = "block";
    });

    ia.addEventListener("click", ()=>{
        pudisplay2.style.display = "block";
    });

    xbtn1.addEventListener("click", ()=>{
        pudisplay1.style.display = "none";
    });

    xbtn2.addEventListener("click", ()=>{
        pudisplay2.style.display = "none";
    });
});