import React from 'react';

export default function CategoryFilter({
  categories = [],
  selectedCategoryId,
  onSelectCategory,
  totalEventsCount,
}) {
  return (
    <div className="category-filter-container">
      <button
        type="button"
        className={`category-pill ${!selectedCategoryId ? 'active' : ''}`}
        onClick={() => onSelectCategory(null)}
      >
        <span className="category-label">All Events</span>
        <span className="category-count">{totalEventsCount}</span>
      </button>

      {categories.map((cat) => (
        <button
          key={cat.id}
          type="button"
          className={`category-pill ${selectedCategoryId === cat.id ? 'active' : ''}`}
          onClick={() => onSelectCategory(cat.id)}
        >
          <span className="category-label">{cat.name}</span>
          <span className="category-count">{cat.eventCount || 0}</span>
        </button>
      ))}
    </div>
  );
}
