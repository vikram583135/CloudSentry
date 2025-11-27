import React, { useRef } from 'react';
import { motion } from 'framer-motion';
import { Canvas, useFrame } from '@react-three/fiber';
import { Sphere, MeshDistortMaterial } from '@react-three/drei';

const AnimatedSphere = () => {
    const mesh = useRef(null);
    useFrame((state) => {
        const t = state.clock.getElapsedTime();
        mesh.current.rotation.x = t * 0.2;
        mesh.current.rotation.y = t * 0.3;
        mesh.current.position.y = Math.sin(t) * 0.1;
    });

    return (
        <Sphere visible args={[1, 100, 200]} scale={2} ref={mesh}>
            <meshStandardMaterial
                color="#00d4ff"
                roughness={0.5}
            />
        </Sphere>
    );
};

const Hero = () => {
    return (
        <header className="hero" style={{ position: 'relative', overflow: 'hidden' }}>
            {/* 3D Background Layer */}
            <div style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', zIndex: 0, opacity: 0.6 }}>
                <Canvas>
                    <ambientLight intensity={0.5} />
                    <directionalLight position={[10, 10, 5]} intensity={1} />
                    <AnimatedSphere />
                </Canvas>
            </div>

            <div className="container" style={{ position: 'relative', zIndex: 1 }}>
                <motion.h1
                    initial={{ opacity: 0, y: -50 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.8 }}
                >
                    Bridging the Gap Between Academic Concepts and Real-World Software.
                </motion.h1>
                <motion.p
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.5, duration: 0.8 }}
                >
                    Premium software solutions for final-year students, budding developers, and startups. We deliver clean code, complete documentation, and 100% execution support.
                </motion.p>
                <motion.div
                    className="btn-group"
                    initial={{ opacity: 0, scale: 0.8 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 1, duration: 0.5 }}
                >
                    <a href="#services" className="btn btn-primary">View Project Catalog</a>
                    <a href="#contact" className="btn btn-secondary">Request Custom Software</a>
                </motion.div>
            </div>
        </header>
    );
};

export default Hero;
