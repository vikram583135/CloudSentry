import React from 'react';

const Navbar = () => {
  return (
    <nav>
        <div className="container nav-flex">
            <h1 className="logo">CodeVantage</h1>
            <ul className="nav-links">
                <li><a href="#about">About</a></li>
                <li><a href="#services">Services</a></li>
                <li><a href="#tech">Tech Stack</a></li>
                <li><a href="#contact">Contact</a></li>
            </ul>
        </div>
    </nav>
  );
};

export default Navbar;
