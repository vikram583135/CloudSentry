import React from 'react';

const WLogo = ({ size = 40, className = '' }) => {
    return (
        <svg
            width={size}
            height={size}
            viewBox="0 0 120 100"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className={className}
        >
            <defs>
                {/* Gradient for the left side (gray/silver) */}
                <linearGradient id="leftGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor="#8B9AAB" />
                    <stop offset="50%" stopColor="#5C6B7A" />
                    <stop offset="100%" stopColor="#3D4A5C" />
                </linearGradient>

                {/* Gradient for the right side (cyan/blue) */}
                <linearGradient id="rightGrad" x1="0%" y1="100%" x2="100%" y2="0%">
                    <stop offset="0%" stopColor="#0284C7" />
                    <stop offset="40%" stopColor="#06B6D4" />
                    <stop offset="100%" stopColor="#22D3EE" />
                </linearGradient>

                {/* Center gradient */}
                <linearGradient id="centerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor="#0EA5E9" />
                    <stop offset="100%" stopColor="#06B6D4" />
                </linearGradient>

                {/* Glow filter */}
                <filter id="glowEffect" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur stdDeviation="2" result="blur" />
                    <feMerge>
                        <feMergeNode in="blur" />
                        <feMergeNode in="SourceGraphic" />
                    </feMerge>
                </filter>

                {/* Strong cyan glow */}
                <filter id="cyanGlow" x="-100%" y="-100%" width="300%" height="300%">
                    <feGaussianBlur stdDeviation="3" result="blur" />
                    <feFlood floodColor="#22D3EE" floodOpacity="0.8" />
                    <feComposite in2="blur" operator="in" />
                    <feMerge>
                        <feMergeNode />
                        <feMergeNode in="SourceGraphic" />
                    </feMerge>
                </filter>
            </defs>

            {/* ===== LEFT V SECTION (Gray/Silver) ===== */}
            <g>
                {/* Main left V strokes - layered for depth */}
                <path
                    d="M 25 15 L 45 70 L 60 40"
                    stroke="url(#leftGrad)"
                    strokeWidth="6"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    fill="none"
                />
                <path
                    d="M 20 18 L 40 68 L 52 45"
                    stroke="#4B5563"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    fill="none"
                    opacity="0.6"
                />

                {/* Circuit traces - left side */}
                <path
                    d="M 28 20 L 15 20 L 15 10"
                    stroke="#6B7280"
                    strokeWidth="2"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 32 30 L 22 30"
                    stroke="#9CA3AF"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 35 40 L 28 40 L 28 50"
                    stroke="#6B7280"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 40 55 L 32 55"
                    stroke="#9CA3AF"
                    strokeWidth="1"
                    strokeLinecap="round"
                    fill="none"
                />

                {/* Circuit nodes - left (gray) */}
                <circle cx="15" cy="10" r="4" fill="#9CA3AF" filter="url(#glowEffect)" />
                <circle cx="15" cy="10" r="2" fill="#D1D5DB" />
                <circle cx="22" cy="30" r="2.5" fill="#6B7280" />
                <circle cx="28" cy="50" r="3" fill="#9CA3AF" />
                <circle cx="28" cy="50" r="1.5" fill="#D1D5DB" />
                <circle cx="32" cy="55" r="1.5" fill="#6B7280" />
            </g>

            {/* ===== RIGHT V SECTION (Cyan/Blue) ===== */}
            <g>
                {/* Main right V strokes - layered for depth */}
                <path
                    d="M 95 15 L 75 70 L 60 40"
                    stroke="url(#rightGrad)"
                    strokeWidth="6"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    fill="none"
                />
                <path
                    d="M 100 18 L 80 68 L 68 45"
                    stroke="#0EA5E9"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    fill="none"
                    opacity="0.5"
                />

                {/* Circuit traces - right side */}
                <path
                    d="M 92 20 L 105 12"
                    stroke="#22D3EE"
                    strokeWidth="2"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 88 30 L 98 30 L 98 22"
                    stroke="#0EA5E9"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 85 40 L 92 40"
                    stroke="#22D3EE"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 82 50 L 90 50 L 90 58"
                    stroke="#0EA5E9"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    fill="none"
                />
                <path
                    d="M 78 60 L 85 60"
                    stroke="#22D3EE"
                    strokeWidth="1"
                    strokeLinecap="round"
                    fill="none"
                />

                {/* Circuit nodes - right (cyan glowing) */}
                <circle cx="105" cy="12" r="5" fill="#22D3EE" filter="url(#cyanGlow)" />
                <circle cx="105" cy="12" r="2.5" fill="#FFFFFF" />
                <circle cx="98" cy="22" r="3" fill="#0EA5E9" filter="url(#glowEffect)" />
                <circle cx="98" cy="22" r="1.5" fill="#67E8F9" />
                <circle cx="92" cy="40" r="2.5" fill="#22D3EE" filter="url(#glowEffect)" />
                <circle cx="90" cy="58" r="4" fill="#22D3EE" filter="url(#cyanGlow)" />
                <circle cx="90" cy="58" r="2" fill="#FFFFFF" />
                <circle cx="85" cy="60" r="2" fill="#0EA5E9" />
            </g>

            {/* ===== CENTER DIAMOND ===== */}
            <path
                d="M 60 32 L 66 45 L 60 58 L 54 45 Z"
                fill="url(#centerGrad)"
                filter="url(#glowEffect)"
            />
            <path
                d="M 60 36 L 63 45 L 60 54 L 57 45 Z"
                fill="#67E8F9"
                opacity="0.6"
            />

            {/* Small accent dots */}
            <circle cx="60" cy="26" r="2" fill="#22D3EE" filter="url(#glowEffect)" />
            <circle cx="48" cy="62" r="1.5" fill="#6B7280" />
            <circle cx="72" cy="62" r="1.5" fill="#0EA5E9" />
        </svg>
    );
};

export default WLogo;
