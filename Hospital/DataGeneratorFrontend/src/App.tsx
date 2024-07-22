import React, { useState } from 'react';
import './App.css';

interface Settings {
    batch_size: string;
    delete_size: string;
    diversity_level: string | number;
}

const App: React.FC = () => {
    const [batch_size, setBatchSize] = useState<string>('');
    const [delete_size, setDeleteSize] = useState<string>('');
    const [diversity_level, setDiversityLevel] = useState<string | number>('random');

    const handleBatchSizeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = event.target.value;
        if (parseInt(value) >= 0 || value === '') {
            setBatchSize(value);
        }
    };

    const handleDeleteSizeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = event.target.value;
        if (parseInt(value) >= 0 || value === '') {
            setDeleteSize(value);
        }
    };

    const handleDiversityLevelChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.value === 'random') {
            setDiversityLevel('random');
        } else {
            setDiversityLevel(Number(event.target.value));
        }
    };

    const setRandomDiversityLevel = () => {
        setDiversityLevel('random');
    };

    const submitSettings = () => {
        const settings: Settings = {
            batch_size: batch_size || 'random',
            delete_size: delete_size || 'random',
            diversity_level
        };

        fetch('http://load-generator-backend:5000/set_parameters', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(settings)
        })
            .then(response => response.json())
            .then(data => console.log('Updated:', data))
            .catch(error => console.error('Error:', error));
    };

    return (
        <div className="App">
            <h1>Data Generator Control Panel</h1>
            <div className="control-group">
                <label>
                    Batch Size:
                    <input type="number" value={batch_size} onChange={handleBatchSizeChange} placeholder="Random" />
                </label>
                <button className="random-button" onClick={() => setBatchSize('')}>Random</button>
            </div>
            <div className="control-group">
                <label>
                    Delete Size:
                    <input type="number" value={delete_size} onChange={handleDeleteSizeChange} placeholder="Random" />
                </label>
                <button className="random-button" onClick={() => setDeleteSize('')}>Random</button>
            </div>
            <div className="control-group">
                <label className="range-group">
                    Disease Diversity Level:
                    <input type="range" value={typeof diversity_level === 'number' ? diversity_level : 50} onChange={handleDiversityLevelChange} min="0" max="100" />
                    <span className="diversity-level">{typeof diversity_level === 'number' ? `${diversity_level}%` : 'Random'}</span>
                </label>
                <button className="random-button" onClick={setRandomDiversityLevel}>Random</button>
            </div>
            <button className="submit-button" onClick={submitSettings}>Apply Settings</button>
        </div>
    );
}

export default App;
