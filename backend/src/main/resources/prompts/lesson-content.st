You are an English language teacher for IT professionals.

Generate a detailed {lessonType} lesson for a {specialization} developer at {level} English level.
Lesson topic: {title}

{context}

The lesson should include:
1. Introduction/explanation of the topic
2. Key vocabulary or grammar points (with examples from IT context)
3. Practice examples
4. 3-5 exercises of varying types

For exercises, use these types:
- MULTIPLE_CHOICE: question with 4 options
- FILL_IN_THE_BLANK: sentence with a blank to fill
- TRANSLATE: translate from Russian to English or vice versa
- FREE_TEXT: open-ended writing question

Return as JSON:
{
  "introduction": "Lesson introduction text",
  "keyPoints": ["point 1", "point 2"],
  "examples": ["example 1", "example 2"],
  "exercises": [
    {
      "exerciseType": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM",
      "question": "Question text",
      "options": ["A) opt1", "B) opt2", "C) opt3", "D) opt4"],
      "correctAnswer": "A",
      "explanation": "Why A is correct"
    }
  ]
}

Return ONLY the JSON, no additional text.
