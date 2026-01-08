import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import WLogo from './WLogo';

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);
  const location = useLocation();

  const navLinks = [
    { name: 'Home', path: '/' },
    { name: 'Projects', path: '/projects' },
    { name: 'About', path: '/about' },
    { name: 'Services', path: '/services' },
    { name: 'Contact', path: '/contact' },
  ];

  return (
    <motion.nav
      className="fixed w-full z-50 overflow-hidden"
      style={{
        fontFamily: 'Outfit, sans-serif',
        background: 'linear-gradient(to bottom, rgba(255,255,255,0.05), rgba(255,255,255,0.02))',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        boxShadow: '0 8px 32px 0 rgba(0, 0, 0, 0.37)'
      }}
      initial={{ height: '60px' }}
      animate={{ height: isExpanded ? '80px' : '60px' }}
      transition={{ duration: 0.3, ease: 'easeInOut' }}
      onMouseEnter={() => setIsExpanded(true)}
      onMouseLeave={() => setIsExpanded(false)}
    >
      <motion.div
        className="container mx-auto px-6 flex justify-between items-center h-full"
        animate={{
          paddingTop: isExpanded ? '20px' : '10px',
          paddingBottom: isExpanded ? '20px' : '10px'
        }}
        transition={{ duration: 0.3 }}
      >
        {/* Logo */}
        <motion.div
          animate={{ scale: isExpanded ? 1.1 : 1 }}
          transition={{ duration: 0.3 }}
        >
          <Link
            to="/"
            className="flex items-center gap-2 hover:scale-105 transition-transform duration-300"
          >
            <WLogo size={42} className="drop-shadow-lg" />
            <span
              className="text-2xl font-extrabold tracking-tight bg-gradient-to-r from-cyan-400 via-blue-500 to-purple-600 bg-clip-text text-transparent hidden sm:inline"
              style={{
                textShadow: '0 0 30px rgba(0, 212, 255, 0.3)',
                letterSpacing: '-0.5px'
              }}
            >
              codeWINtage
            </span>
          </Link>
        </motion.div>

        {/* Desktop Menu */}
        <motion.ul
          className="hidden md:flex items-center gap-10"
          animate={{ opacity: isExpanded ? 1 : 0.9 }}
          transition={{ duration: 0.3 }}
        >
          {navLinks.map((link, index) => (
            <motion.li
              key={link.name}
              initial={{ opacity: 0, y: -10 }}
              animate={{
                opacity: isExpanded ? 1 : 0.9,
                y: isExpanded ? 0 : -5
              }}
              transition={{
                duration: 0.3,
                delay: isExpanded ? index * 0.05 : 0
              }}
            >
              <Link
                to={link.path}
                className={`text-base font-medium transition-all duration-300 relative group ${location.pathname === link.path
                  ? 'text-white'
                  : 'text-gray-300 hover:text-white'
                  }`}
                style={{
                  textShadow: location.pathname === link.path ? '0 0 10px rgba(0, 212, 255, 0.5)' : 'none'
                }}
              >
                {link.name}
                {/* Active underline */}
                <span
                  className={`absolute -bottom-1 left-0 h-0.5 bg-gradient-to-r from-cyan-400 to-blue-500 transition-all duration-300 ${location.pathname === link.path ? 'w-full' : 'w-0 group-hover:w-full'
                    }`}
                  style={{
                    boxShadow: location.pathname === link.path ? '0 0 10px rgba(0, 212, 255, 0.5)' : 'none'
                  }}
                />
              </Link>
            </motion.li>
          ))}
        </motion.ul>

        {/* Mobile Menu Button */}
        <button
          className="md:hidden text-white hover:text-cyan-400 transition-colors"
          onClick={() => setIsOpen(!isOpen)}
          aria-label="Toggle menu"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={isOpen ? "M6 18L18 6M6 6l12 12" : "M4 6h16M4 12h16M4 18h16"} />
          </svg>
        </button>
      </motion.div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="md:hidden overflow-hidden"
            style={{
              background: 'linear-gradient(to bottom, rgba(255,255,255,0.08), rgba(255,255,255,0.03))',
              backdropFilter: 'blur(20px)',
              WebkitBackdropFilter: 'blur(20px)',
              borderBottom: '1px solid rgba(255,255,255,0.1)'
            }}
          >
            <ul className="flex flex-col p-6 gap-4">
              {navLinks.map((link) => (
                <li key={link.name}>
                  <Link
                    to={link.path}
                    onClick={() => setIsOpen(false)}
                    className={`block text-lg font-medium transition-all ${location.pathname === link.path
                      ? 'text-white px-4 py-2 rounded-lg border-l-2 border-cyan-400'
                      : 'text-gray-300 hover:text-white px-4 py-2'
                      }`}
                    style={{
                      background: location.pathname === link.path
                        ? 'linear-gradient(to right, rgba(0, 212, 255, 0.1), rgba(112, 0, 255, 0.1))'
                        : 'transparent'
                    }}
                  >
                    {link.name}
                  </Link>
                </li>
              ))}
            </ul>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.nav>
  );
};

export default Navbar;
