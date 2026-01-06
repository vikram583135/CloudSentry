
document.addEventListener("DOMContentLoaded", ()=>{
    /*page 1 features */
    const sem1 = document.getElementById("SEM1");
    const sem2 = document.getElementById("SEM2");
    const sem3 = document.getElementById("SEM3");
    const firsttt = document.getElementById("FIRST-TT");
    const thirdtt= document.getElementById("THIRD-TT");
    const timetable = document.getElementById("TIMETABLE");
    const pudisplay1 = document.getElementById("P-U-DISPLAY1");
    const xbtn = document.getElementById("X-BTN");
    const deptname = document.querySelector(".dept-name");

    sem1.addEventListener("click", ()=>{
        localStorage.setItem("semester", "f");
        window.location.href = "./page2.html";
    });

    sem2.addEventListener("click", ()=>{
        localStorage.setItem("semester", "s");
        window.location.href = "./page2.html";
    });

    sem3.addEventListener("click", ()=>{
        localStorage.setItem("semester", "t");
        window.location.href = "./page2.html";
    });

    pudisplay1.style.display = "none";
    firsttt.style.display="none"; 
    thirdtt.style.display="none";
    timetable.addEventListener("change", (event)=>{
        const x = event.target.value;
        switch(x){
            case "i" : pudisplay1.style.display = "block"; 
                        firsttt.style.display="block"; 
                        break;
            case "ii" : alert("selected 2 sem"); break;
            case "iii" : pudisplay1.style.display = "block";
                         thirdtt.style.display="block";
                         break;
        }
    });

    xbtn.addEventListener("click", ()=>{
        firsttt.style.display="none"; 
        thirdtt.style.display="none";
        pudisplay1.style.display = "none";
    });

    deptname.addEventListener("click", ()=>{
        window.open("https://maps.app.goo.gl/FWQHnbsWujfkC2jC7?g_st=aw", "_blank")
    })






    


});