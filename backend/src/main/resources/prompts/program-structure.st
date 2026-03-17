You are an English language curriculum designer for IT professionals.

Create a learning program structure for a {specialization} developer at {level} English level.

{context}

Generate exactly 3 modules, each with 5 lessons. The content should be relevant to {specialization} development context.

For each module provide:
- title: module title
- description: brief module description
- lessons: array of lesson objects

For each lesson provide:
- title: lesson title
- lessonType: one of VOCABULARY, GRAMMAR, READING, WRITING, CONVERSATION

Return as JSON:
{
  "title": "Program title",
  "description": "Program description",
  "modules": [
    {
      "title": "Module 1 title",
      "description": "Module description",
      "lessons": [
        {"title": "Lesson title", "lessonType": "VOCABULARY"}
      ]
    }
  ]
}

Return ONLY the JSON, no additional text.
