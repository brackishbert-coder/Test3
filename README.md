# test3 — SOM Layer 1

The **first neural layer** of the [Distributed Chess System](../). Takes visual input,
**vectorizes** it, and folds it through a **reversible, nested self-organizing map** into a
structured intermediate representation that the second layer (test4) consumes.

## What it does

- **Vectorize** — `FeatureVectorizer` / `ImageVectorizer` / `ClientVectorizer` turn input
  (image blocks / features) into numeric vectors.
- **Organize** — a `BaseSOM` → `MetaSOM` hierarchy maps the vector cloud onto a 2-D
  organization where similar inputs land near each other.
- **Reverse** — `ReversibleSOMHierarchy` keeps the mapping invertible: *forward* (vector →
  winning node) and *backward* (node → representative vector), so the layer can be read both
  ways.
- **Visualize** — `SOMHierarchyVisualizer`, `SOMVisualizationPanel`, `IndividualSOMPanel`
  render the maps live as they train (Swing).

## Dependencies

- **JDK 17**, **Maven**. Standard library only (Swing/AWT) — no third-party dependencies.

## Build & run

```bash
mvn compile

# run one of the entry points:
java -cp target/classes test3.SOMHierarchyVisualizer
# or:  java -cp target/classes test3.ReversibleSOMHierarchy
# or:  java -cp target/classes test3.ImageVectorizer
```

## Structure

```
test3/
├── pom.xml
└── src/test3/
    ├── FeatureVectorizer.java / ImageVectorizer.java / ClientVectorizer.java   input → vectors
    ├── BaseSOM.java / MetaSOM.java                                             the SOM levels
    ├── ReversibleSOMHierarchy.java                                             invertible hierarchy  [main]
    ├── SOMHierarchyVisualizer.java                                             top-level viewer       [main]
    └── SOMVisualizationPanel.java / IndividualSOMPanel.java                    Swing rendering
```

---

*Part of the Distributed Chess System: this layer organizes what is **seen**; test4 organizes
what may be **done**.*
