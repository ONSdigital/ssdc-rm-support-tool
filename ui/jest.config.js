export default {
    transformIgnorePatterns: [
        "node_modules/(?!(axios)/)"
    ],
    transform: {
        '^.+\\.tsx?$': 'babel-jest',
    }
}