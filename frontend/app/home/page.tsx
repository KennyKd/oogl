import { useState } from "react";
import { fetchAutocomplete } from "../../lib/fetchAutocomplete";

export default function Home() {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<string[]>([]);

  const handleInputChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
      const value = event.target.value;
      setQuery(value);
      if (value.length > 0) {
          const results = await fetchAutocomplete(value);
          setSuggestions(results);
      } else {
          setSuggestions([]);
      }
  };

  return (
    <div className=" justify-center content-center items-center h-screen"> 
      <div className="flex flex-col items-center justify-center m-auto">
        <h2 className="text-2xl font-semibold">With Trie</h2>
        <input className="rounded-l-full rounded-r-full p-4 w-80 text-gray-800" type="text" value={query} onChange={handleInputChange} placeholder="Start typing..." />
        <ul>
          {suggestions.map((word, index) => (
            <li key={index}>{word}</li>
          ))}
        </ul>
      </div>
      <div className="flex flex-col items-center justify-center m-auto mt-8">
        <h2 className="text-2xl font-semibold">With Ternary Search</h2>
        <input className="rounded-l-full rounded-r-full p-4 w-80 text-gray-800" type="text" id="ts" placeholder="Start typing..." />
      </div>
    </div>
  );
}
