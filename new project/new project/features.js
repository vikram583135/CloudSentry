function nextPage(){
    window.location.href = "./page2.html";
}
function goToHome(){
    window.location.href = "./index.html";
}
function semOne(){
    nextPage();
}
function semTwo(){
    nextPage();
}
function semThree(){
    nextPage();
}
function semFour(){
    nextPage();
}
function sub1Button(){
    document.getElementById("divTB").innerHTML = '<a href="textbook/agile-software-engineering.pdf" download="TB_software_engineering">Download Software Engineering Textbook</a>';
    document.getElementById("divNts").innerHTML = '<a href="textbook/" download="software_engineering_notes">Download Software Engineering notes</a>';
    document.getElementById("divQP").innerHTML = '<a href="textbook/" download="software_engineering_QP">Download Software Engineering papers</a>';
}
function sub2Button(){
    document.getElementById("divTB").innerHTML = '<a href="textbook/AI_Russell_Norvig.pdf" download="TB_Artificial_Intelligence">Download Artificial Intelligence Textbook</a>';
    document.getElementById("divNts").innerHTML = '<a href="textbook/" download="Artificial_Intelligence_notes">Download Artificial Intelligence notes</a>';
    document.getElementById("divQP").innerHTML = '<a href="textbook/" download="Artificial_Intelligence_QP">Download Artificial Intelligence papers</a>';
}
function sub3Button(){
    document.getElementById("divTB").innerHTML = '<a href="textbook/Data-Communications-and-Networking-By-Behrouz-A.Forouzan.pdf" download="TB_Computer_Network">Download Computer Network Textbook</a>';
    document.getElementById("divNts").innerHTML = '<a href="textbook/" download="Computer_Network_notes">Download Computer Network notes</a>';
    document.getElementById("divQP").innerHTML = '<a href="textbook/" download="Computer_Network_QP">Download Computer Network papers</a>';
}
function sub4Button(){
    document.getElementById("divTB").innerHTML = '<a href="textbook/Silberschatz 9th edition.pdf" download="TB_Operating_system">Download Operating System Textbook</a>';
    document.getElementById("divNts").innerHTML = '<a href="textbook/" download="Operating_system_notes">Download Operating System notes</a>';
    document.getElementById("divQP").innerHTML = '<a href="textbook/" download="Operating_system_QP">Download Operating System papers</a>';
}
function sub5Button(){
    document.getElementById("divTB").innerHTML = '<a href="textbook/Fundamentals_of_Database_Systems_6th_Edition-1.pdf" download="TB_DBMS">Download DBMS Textbook</a>';
    document.getElementById("divNts").innerHTML = '<a href="textbook/" download="DBMS_notes">Download DBMS notes</a>';
    document.getElementById("divQP").innerHTML = '<a href="textbook/" download="DBMS_QP">Download DBMS papers</a>';
}
function clickEffect(){
    document.getElementsByClassName("nav_btn").style.borderWidth = "2px";
    document.getElementsByClassName("nav_btn").style.backgroundColor = "#997c70";
    document.getElementsByClassName("nav_btn").style.color = "white";
}