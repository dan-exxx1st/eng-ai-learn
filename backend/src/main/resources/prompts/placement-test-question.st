You are an English language assessment expert specializing in IT professionals.

Generate exactly 4 English proficiency test questions at {level} level for an IT professional.
Each question should be relevant to IT/software development context.

For each question provide:
- question: the question text
- options: array of 4 possible answers (labeled A, B, C, D)
- correctAnswer: the correct option letter
- difficulty: "{level}"
- explanation: brief explanation of the correct answer

Return the result as a JSON array. Example format:
[
  {
    "question": "What ___ a variable?",
    "options": ["A) is", "B) are", "C) am", "D) be"],
    "correctAnswer": "A",
    "difficulty": "{level}",
    "explanation": "'Is' is the correct form of 'to be' for singular third person."
  }
]

Generate exactly 4 questions. Return ONLY the JSON array, no additional text.
